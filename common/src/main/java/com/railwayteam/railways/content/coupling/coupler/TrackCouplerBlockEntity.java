/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.coupling.coupler;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.coupling.TrainUtils;
import com.railwayteam.railways.mixin.AccessorTrackTargetingBehavior;
import com.railwayteam.railways.mixin_interfaces.IOccupiedCouplers;
import com.railwayteam.railways.multiloader.PlayerSelection;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.multiloader.Env;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import com.railwayteam.railways.util.packet.TrackCouplerClientInfoPacket;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public class TrackCouplerBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

    private BlockState cachedTrackState = null;
    private BlockState cachedSecondaryTrackState = null;
    private boolean edgePointsOk = false;
    private boolean lastReportedPower = false;
    private int lastAnalogOutput = 0;
    protected int edgeSpacing = 5;
    private int lastEdgeSpacing = 5;
    private boolean debugCouplerOperation = false;
    private MutableComponent error = null;
    private MutableComponent error2 = null;
    private ClientInfo clientInfo;

    public TrackTargetingBehaviour<TrackCoupler> edgePoint;
    public TrackTargetingBehaviour<TrackCoupler> secondEdgePoint;
    protected ServerScrollValueBehaviour edgeSpacingScroll;

    protected int cachedEffectiveEdgeSpacing = 5;

    public TrackCouplerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.putBoolean("EdgePointsOk", edgePointsOk);
        output.putBoolean("Power", lastReportedPower);
        output.putInt("AnalogOutput", lastAnalogOutput);
        output.putInt("EdgeSpacing", edgeSpacing);
        output.putInt("LastEdgeSpacing", lastEdgeSpacing);
        //if (clientPacket && clientInfo != null)
        //    output.store("ClientInfo", CompoundTag.CODEC, clientInfo.write());
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        edgePointsOk = input.getBooleanOr("EdgePointsOk", false);
        lastReportedPower = input.getBooleanOr("Power", false);
        lastAnalogOutput = input.getIntOr("AnalogOutput", 0);
        edgeSpacing = input.getIntOr("EdgeSpacing", edgeSpacing);
        lastEdgeSpacing = input.getIntOr("LastEdgeSpacing", lastEdgeSpacing);
        edgeSpacingScroll.setValue(edgeSpacing);
        //if (clientPacket)
        //    input.read("ClientInfo", CompoundTag.CODEC).ifPresent(tag -> clientInfo = new ClientInfo(tag));
        invalidateRenderBoundingBox();
    }
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(edgePoint = new TrackTargetingBehaviour<>(this, CREdgePointTypes.COUPLER));
        behaviours.add(secondEdgePoint = new SecondaryTrackTargetingBehaviour<>(this, CREdgePointTypes.COUPLER));
        edgeSpacingScroll = new ServerScrollValueBehaviour(this) {
            public String getClipboardKey() {
                return "Coupler";
            }
        };
        edgeSpacingScroll.between(3, 15);
        edgeSpacingScroll.withCallback(i -> this.edgeSpacing = i);
        behaviours.add(edgeSpacingScroll);
        Env.CLIENT.runIfCurrent(() -> () -> behaviours.add(TrackCouplerBlockEntityClientBehaviours.createEdgeSpacingScroll(this)));
    }
    public void tick() {
        super.tick();

        if (level.isClientSide())
            return;

        BlockState blockState = getBlockState();
        blockState.getOptionalValue(TrackCouplerBlock.POWERED).ifPresent(powered -> {
            if (lastReportedPower == powered)
                return;
            Railways.LOGGER.info("[TrackCoupler {}] block entity power transition previous={} current={} mode={} edgePointsOk={}",
                getBlockPos(), lastReportedPower, powered, getAllowedOperationMode(), edgePointsOk);
            lastReportedPower = powered;
            if (powered)
                onPowered();
            else
                onUnpowered();
            notifyUpdate();
        });

        if (getTargetAnalogOutput() != lastAnalogOutput) {
            lastAnalogOutput = getTargetAnalogOutput();
            level.updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
        }
//        DisplayLinkBlock.notifyGatherers(level, worldPosition);
    }

    protected void onPowered() {
        if (level == null || level.isClientSide())
            return;
//        this.getSecondaryCoupler().blockEntityAdded(this, false); //FIX_ME remove this
        debugCouplerOperation = true;
        try {
            debugCoupler("powered: allowedMode={} edgePointsOk={} spacing={} primaryTarget={} secondaryTarget={} primaryGlobal={} secondaryGlobal={}",
                getAllowedOperationMode(), edgePointsOk, edgeSpacing, getTargetTrack(edgePoint), getTargetTrack(secondEdgePoint),
                edgePoint.getGlobalPosition(), secondEdgePoint.getGlobalPosition());
            refreshOccupiedCouplers();
            OperationInfo info = getOperationInfo();
            debugCoupler("resolved operation={}", info.mode);
            switch (info.mode) {
                case DECOUPLING -> {
                    Train train = info.frontCarriage.train;
                    int splitIndex = train.carriages.indexOf(info.backCarriage);
                    int numberOffEnd = train.carriages.size() - splitIndex; // all carriages after and including the back carriage
                    debugCoupler("decoupling: train={} frontIndex={} backIndex={} numberOffEnd={}",
                        describeTrain(train), train.carriages.indexOf(info.frontCarriage), splitIndex, numberOffEnd);
                    TrainUtils.splitTrain(train, numberOffEnd);
                }
                case COUPLING -> {
                    Train frontTrain = info.frontCarriage.train;
                    Train backTrain = info.backCarriage.train;
                    debugCoupler("coupling: frontTrain={} backTrain={} effectiveSpacing={}",
                        describeTrain(frontTrain), describeTrain(backTrain), cachedEffectiveEdgeSpacing);
                    if (frontTrain == backTrain) {
                        debugCoupler("coupling skipped: both carriage references are on the same train");
                        break;
                    }
                    TrainUtils.combineTrains(frontTrain, backTrain, getBlockPos().above(), level, cachedEffectiveEdgeSpacing);
                }
                case NONE -> debugCoupler("no operation: error={} secondaryError={}", describeComponent(error), describeComponent(error2));
            }
        } finally {
            debugCouplerOperation = false;
        }
    }

    protected void onUnpowered() {
    }

    private void refreshOccupiedCouplers() {
        TrackGraphLocation loc1 = edgePoint.determineGraphLocation();
        TrackGraphLocation loc2 = secondEdgePoint.determineGraphLocation();
        if (loc1 == null || loc1.graph == null) {
            debugCoupler("refresh skipped: primary graph location missing");
            return;
        }

        TrackGraph graph = loc1.graph;
        if (loc2 != null && loc2.graph != null && loc2.graph != graph) {
            debugCoupler("refresh skipped: primary and secondary are on different graphs");
            return;
        }

        int[] refreshed = {0};
        Create.RAILWAYS.trains.forEach((uuid, train) -> {
            if (train.graph != graph)
                return;
            train.collectInitiallyOccupiedSignalBlocks();
            refreshed[0]++;
            if (train instanceof IOccupiedCouplers occupiedCouplers) {
                debugCoupler("refresh train={} occupiedCouplers={}", describeTrain(train), occupiedCouplers.railways$getOccupiedCouplers());
                for (UUID couplerId : occupiedCouplers.railways$getOccupiedCouplers()) {
                    TrackCoupler coupler = graph.getPoint(CREdgePointTypes.COUPLER, couplerId);
                    if (coupler != null) {
                        coupler.keepAlive(train);
                        debugCoupler("kept alive coupler={} for train={}", describeCoupler(coupler), describeTrain(train));
                    }
                }
            }
        });
        debugCoupler("refresh complete: trainsOnGraph={}", refreshed[0]);
    }

    public boolean getReportedPower() {
        return lastReportedPower;
    }

    public int getEdgeSpacing() {
        return edgeSpacing;
    }

    private Optional<BlockPos> getDesiredSecondaryEdgePos() {
        BlockState trackState = edgePoint.getTrackBlockState();
        if (!trackState.hasProperty(TrackBlock.SHAPE))
            return Optional.empty();

        double distance = -getEdgeSpacing() * edgePoint.getTargetDirection().getStep();
        Vec3 axis = trackState.getValue(TrackBlock.SHAPE).getAxes().get(0);
        Vec3 offset = axis.scale(distance);
        cachedEffectiveEdgeSpacing = (int) Math.round(edgeSpacing * axis.length());
        return Optional.of(((AccessorTrackTargetingBehavior) edgePoint).getTargetTrack().offset(Mth.floor(offset.x), Mth.floor(offset.y), Mth.floor(offset.z)));
    }

    private @Nullable
    BlockState getSecondaryTrackState() {
        return getDesiredSecondaryEdgePos().map(pos -> level.getBlockState(pos.offset(this.getBlockPos()))).orElse(null);
    }

    private void setError(Component component) {
        error = Component.empty().append(component);
    }

    private void setError2(Component component) {
        error2 = Component.empty().append(component);
    }

    private void clearErrors() {
        error = null;
    }

    private void clearError2() {
        error2 = null;
    }

    private final int lazierTickRate = 6;
    private int lazierTickCounter = 0;
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide())
            return;
        BlockState trackState = edgePoint.getTrackBlockState();
        BlockState secondaryTrackState = getSecondaryTrackState();
        if (trackState != cachedTrackState || secondaryTrackState != cachedSecondaryTrackState || edgeSpacing != lastEdgeSpacing) {
            invalidateRenderBoundingBox();
            cachedTrackState = trackState;
            cachedSecondaryTrackState = secondaryTrackState;
            lastEdgeSpacing = edgeSpacing;
            BlockPos newPos = isOkExceptGraph() ? getDesiredSecondaryEdgePos().orElse(BlockPos.ZERO) : BlockPos.ZERO;
            if (!newPos.equals(((AccessorTrackTargetingBehavior) secondEdgePoint).getTargetTrack())) {
                ((AccessorTrackTargetingBehavior) secondEdgePoint).setTargetTrack(newPos);
                TrackCoupler point = secondEdgePoint.getEdgePoint();
                if (point != null && secondEdgePoint.hasValidTrack()) { // don't want to try anything if not on an actual track
                    TrackGraphLocation location = secondEdgePoint.determineGraphLocation();
                    if (location != null && location.graph != null) {
                        location.graph.removePoint(level.getServer(), CREdgePointTypes.COUPLER, point.id);
                        Create.RAILWAYS.trains.forEach((uuid, train) -> {
                            if (train instanceof IOccupiedCouplers occupiedCouplers)
                                occupiedCouplers.railways$getOccupiedCouplers().remove(point.id);
                            if (uuid.equals(point.getCurrentTrain()) || train.graph == location.graph) {
                                train.updateSignalBlocks = true;
                            }
                        });
                    }
                }
                ((AccessorTrackTargetingBehavior) secondEdgePoint).setEdgePoint(null);
//                secondEdgePoint.edgePoint = secondEdgePoint.createEdgePoint();  // - this is taken care of by the behaviour's tick method
                if (isOkExceptGraph())
                    ((AccessorTrackTargetingBehavior) secondEdgePoint).setTargetDirection(((AccessorTrackTargetingBehavior) edgePoint).getTargetDirection().opposite());
                sendData();
            }
        }
        if (lazierTickCounter-- <= 0) {
            lazierTickCounter = lazierTickRate;
            clearError2();
            updateOK();
        }
        clientInfo = new ClientInfo(this);
        clearErrors();
        if (level instanceof ServerLevel serverLevel) {
            CRPackets.PACKETS.sendTo(PlayerSelection.tracking(serverLevel, getBlockPos()), new TrackCouplerClientInfoPacket(this));
        }
    }

    private boolean isOkExceptGraph() {
        return cachedTrackState.getBlock() instanceof ITrackBlock && cachedSecondaryTrackState.getBlock() instanceof ITrackBlock &&
                cachedTrackState.hasProperty(TrackBlock.SHAPE) && cachedSecondaryTrackState.hasProperty(TrackBlock.SHAPE) &&
                cachedTrackState.getValue(TrackBlock.SHAPE) == cachedSecondaryTrackState.getValue(TrackBlock.SHAPE);
    }

    protected void updateOK() {
        if (!isOkExceptGraph()) {
            setError2(Component.literal("Wrong blocks or track shapes"));
            edgePointsOk = false;
            return;
        }
        if (((AccessorTrackTargetingBehavior) secondEdgePoint).getTargetTrack().equals(BlockPos.ZERO) || ((AccessorTrackTargetingBehavior) edgePoint).getTargetTrack().equals(BlockPos.ZERO)) {
            setError2(Component.literal("Missing edge point(s)"));
            edgePointsOk = false;
            return;
        }

        TrackGraphLocation loc1 = edgePoint.determineGraphLocation();
        TrackGraphLocation loc2 = secondEdgePoint.determineGraphLocation();
        if (loc1 == null || loc2 == null) {
            setError2(Component.literal("Edge point(s) missing graph location"));
            edgePointsOk = false;
            return;
        }

        if (loc1.graph != loc2.graph) {
            setError2(Component.literal("Edge points not on same graph"));
            edgePointsOk = false;
            return;
        }

        //check that edgePoint and secondEdgePoint are on the same or adjacent TrackEdge
        Couple<TrackNodeLocation> edgePointLocations = loc1.edge;
        Couple<TrackNodeLocation> secondEdgePointLocations = loc2.edge;
        if (edgePointLocations == null || secondEdgePointLocations == null) {
            setError2(Component.literal("Edge point(s) missing edge location"));
            edgePointsOk = false;
            return;
        }
        if (CRConfigs.server().strictCoupler.get()) {
            edgePointsOk = edgePointLocations.getFirst().equals(secondEdgePointLocations.getFirst()) || edgePointLocations.getFirst().equals(secondEdgePointLocations.getSecond()) ||
                    edgePointLocations.getSecond().equals(secondEdgePointLocations.getFirst()) || edgePointLocations.getSecond().equals(secondEdgePointLocations.getSecond());
            if (!edgePointsOk)
                setError2(Component.literal("Edge points not on same or adjacent edges"));
        } else {
            edgePointsOk = true;
        }
    }

    public boolean areEdgePointsOk() {
        return (level != null && level.isClientSide() && clientInfo != null) ? clientInfo.edgePointsOk : edgePointsOk;
    }

    @Nullable
    public TrackCoupler getCoupler() {
        return edgePoint.getEdgePoint();
    }

    @Nullable
    public TrackCoupler getSecondaryCoupler() {
        return secondEdgePoint.getEdgePoint();
    }

    /**
     * Carriage must have its wheels on the point for it to count
     */
    @Nullable
    protected Carriage getCarriageOnPoint(@NotNull Train train, @NotNull TrackCoupler coupler, @NotNull TrackTargetingBehaviour<TrackCoupler> edgePoint, boolean leading) {
        for (Carriage carriage : train.carriages) {
            if (isCarriageWheelOnPoint(carriage, coupler, edgePoint, leading))
                return carriage;
        }
        return null;
    }

    protected boolean isCarriageWheelOnPoint(Carriage carriage, TrackCoupler coupler, TrackTargetingBehaviour<TrackCoupler> edgePoint, boolean leading) {
        TravellingPoint relevantPoint = leading ? carriage.leadingBogey().leading() : carriage.trailingBogey().trailing();
        TravellingPoint relevantPoint2 = leading ? carriage.leadingBogey().trailing() : carriage.trailingBogey().leading();
        CarriageBogey relevantBogey = leading ? carriage.leadingBogey() : carriage.trailingBogey();
        boolean upsideDown = relevantBogey.isUpsideDown();
        double couplerPosition = coupler.getLocationOn(relevantPoint.edge);
        Vec3 wheelPosition = relevantPoint.getPosition(carriage.train.graph).add(relevantPoint2.getPosition(carriage.train.graph)).scale(0.5).add(0, upsideDown ? 2 : 0, 0);
        Vec3 couplerSpatialPosition = Vec3.atBottomCenterOf(edgePoint.getGlobalPosition().above());
//        return (coupler.isPrimary(relevantPoint.node1) || coupler.isPrimary(relevantPoint.node2)) && Math.abs(relevantPoint.position - (couplerPosition+0.5)) < .75;
        boolean primarySide = coupler.isPrimary(relevantPoint.node1) || coupler.isPrimary(relevantPoint.node2) ||
                coupler.isPrimary(relevantPoint2.node1) || coupler.isPrimary(relevantPoint2.node2);
        double distanceSqr = wheelPosition.distanceToSqr(couplerSpatialPosition);
        boolean result = primarySide && distanceSqr < .8 * .8;
        debugCoupler("carriage check: train={} carriageIndex={} leading={} target={} coupler={} couplerPosition={} primarySide={} distanceSqr={} result={}",
            describeTrain(carriage.train), carriage.train.carriages.indexOf(carriage), leading, edgePoint.getGlobalPosition(),
            describeCoupler(coupler), couplerPosition, primarySide, distanceSqr, result);
        return result;
    }

    public AllowedOperationMode getAllowedOperationMode() {
        return getBlockState().getValue(TrackCouplerBlock.MODE);
    }

    public OperationInfo getOperationInfo() {
        clearErrors();
        OperationInfo info = getOperationInfo(false);
        debugCoupler("normal orientation result={}", info.mode);
        if (info.mode == OperationMode.NONE) {
            MutableComponent backupError = error;
            clearErrors();
            info = getOperationInfo(true);
            debugCoupler("reversed orientation result={}", info.mode);
            if (info.mode == OperationMode.NONE)
                error = backupError;
        }
        if (!info.mode.permitted(getAllowedOperationMode())) {
            clearErrors();
            setError(Component.translatable("railways.tooltip.coupler.error.mode_not_permitted"));
            debugCoupler("operation {} blocked by allowed mode {}", info.mode, getAllowedOperationMode());
            return OperationInfo.NONE;
        }
        return info;
    }

    protected OperationInfo getOperationInfo(boolean reversed) {
        TrackCoupler coupler1 = reversed ? getSecondaryCoupler() : getCoupler();
        TrackCoupler coupler2 = reversed ? getCoupler() : getSecondaryCoupler();

        TrackTargetingBehaviour<TrackCoupler> edgePoint1 = reversed ? secondEdgePoint : edgePoint;
        TrackTargetingBehaviour<TrackCoupler> edgePoint2 = reversed ? edgePoint : secondEdgePoint;
        debugCoupler("checking orientation reversed={} coupler1={} coupler2={} edgePoint1={} edgePoint2={}",
            reversed, describeCoupler(coupler1), describeCoupler(coupler2), edgePoint1.getGlobalPosition(), edgePoint2.getGlobalPosition());
        if (coupler1 != null && coupler2 != null && coupler1.isActivated() && coupler2.isActivated()) {
            Train primaryTrain = Create.RAILWAYS.trains.get(coupler1.getCurrentTrain());
            Train secondaryTrain = Create.RAILWAYS.trains.get(coupler2.getCurrentTrain());
            debugCoupler("activated trains: primary={} secondary={}", describeTrain(primaryTrain), describeTrain(secondaryTrain));
            if (primaryTrain != null && primaryTrain == secondaryTrain) {
                //Decoupling, if back wheels of a carriage are on the secondary coupler and the front wheels of the carriage behind it are on the primary coupler
                Carriage frontCarriage = getCarriageOnPoint(primaryTrain, coupler2, edgePoint2, false);
                if (frontCarriage == null)
                    setError(Component.translatable("railways.tooltip.coupler.error.carriage_alignment"));
                debugCoupler("decouple candidate: frontCarriageIndex={}",
                    frontCarriage == null ? "null" : primaryTrain.carriages.indexOf(frontCarriage));
                if (frontCarriage != null && primaryTrain.carriages.indexOf(frontCarriage) < primaryTrain.carriages.size() - 1) {
                    Carriage backCarriage = primaryTrain.carriages.get(primaryTrain.carriages.indexOf(frontCarriage) + 1);
                    boolean backAligned = isCarriageWheelOnPoint(backCarriage, coupler1, edgePoint1, true);
                    int indexGap = Math.abs(primaryTrain.carriages.indexOf(frontCarriage) - primaryTrain.carriages.indexOf(backCarriage));
                    debugCoupler("decouple candidate: backCarriageIndex={} backAligned={} indexGap={}",
                        primaryTrain.carriages.indexOf(backCarriage), backAligned, indexGap);
                    if (backAligned && indexGap == 1) //Make sure that the carriages are actually next to each other
                        return new OperationInfo(OperationMode.DECOUPLING, frontCarriage, backCarriage);
                    else
                        setError(Component.translatable("railways.tooltip.coupler.error.carriage_alignment"));
                } else {
                    setError(Component.translatable("railways.tooltip.coupler.error.carriage_alignment"));
                }
            } else if (primaryTrain != null && secondaryTrain != null) {
                //Coupling if the front wheels of primaryTrain are on coupler1 and the back wheels of secondaryTrain are on coupler2
                Carriage primaryCarriage = getCarriageOnPoint(primaryTrain, coupler1, edgePoint1, true);
                Carriage secondaryCarriage = getCarriageOnPoint(secondaryTrain, coupler2, edgePoint2, false);
                debugCoupler("couple candidate: primaryCarriageIndex={} secondaryCarriageIndex={}",
                    primaryCarriage == null ? "null" : primaryTrain.carriages.indexOf(primaryCarriage),
                    secondaryCarriage == null ? "null" : secondaryTrain.carriages.indexOf(secondaryCarriage));
                if (primaryCarriage != null && secondaryCarriage != null && primaryTrain.carriages.indexOf(primaryCarriage) == 0 &&
                        secondaryTrain.carriages.indexOf(secondaryCarriage) == secondaryTrain.carriages.size() - 1) {
                    // ensure correct order when only one bogey (if 'outer' points are closer together than 'inner' points, then something is off
                    double outerLength = secondaryCarriage.getLeadingPoint().getPosition(secondaryTrain.graph)
                        .subtract(primaryCarriage.getTrailingPoint().getPosition(primaryTrain.graph))
                        .lengthSqr();
                    double innerLength = secondaryCarriage.getTrailingPoint().getPosition(secondaryTrain.graph)
                        .subtract(primaryCarriage.getLeadingPoint().getPosition(primaryTrain.graph))
                        .lengthSqr();
                    debugCoupler("couple candidate lengths: outer={} inner={}", outerLength, innerLength);
                    if (outerLength < innerLength) {
                        return OperationInfo.NONE;
                    } else {
                        return new OperationInfo(OperationMode.COUPLING, secondaryCarriage, primaryCarriage);
                    }
                } else {
                    if (primaryCarriage != null && getCarriageOnPoint(secondaryTrain, coupler2, edgePoint2, true) != null) {
                        setError(Component.translatable("railways.tooltip.coupler.error.carriage_orientation"));
                    } else if (secondaryCarriage != null && getCarriageOnPoint(primaryTrain, coupler1, edgePoint1, false) != null) {
                        setError(Component.translatable("railways.tooltip.coupler.error.carriage_orientation"));
                    } else {
                        setError(Component.translatable("railways.tooltip.coupler.error.carriage_alignment"));
                    }
                }
            } else {
                setError(Component.translatable("railways.tooltip.coupler.error.missing_train"));
            }
        } else {
            setError(Component.translatable("railways.tooltip.coupler.error.missing_train"));
        }
        return OperationInfo.NONE;
    }

    private @Nullable BlockPos getTargetTrack(TrackTargetingBehaviour<TrackCoupler> target) {
        if (target == null)
            return null;
        return ((AccessorTrackTargetingBehavior) target).getTargetTrack();
    }

    private void debugCoupler(String message, Object... args) {
        if (!debugCouplerOperation)
            return;
        Object[] allArgs = new Object[args.length + 1];
        allArgs[0] = getBlockPos();
        System.arraycopy(args, 0, allArgs, 1, args.length);
        Railways.LOGGER.info("[TrackCoupler {}] " + message, allArgs);
    }

    private String describeCoupler(@Nullable TrackCoupler coupler) {
        if (coupler == null)
            return "null";
        return "id=" + coupler.getId() + ",active=" + coupler.isActivated() + ",train=" + coupler.getCurrentTrain();
    }

    private String describeTrain(@Nullable Train train) {
        if (train == null)
            return "null";
        return train.id + "/" + train.name.getString() + ",carriages=" + train.carriages.size();
    }

    private String describeComponent(@Nullable Component component) {
        return component == null ? "null" : component.getString();
    }

    public OperationMode getOperationMode() {
        return getOperationInfo().mode;
    }

    public ClientInfo getClientInfo() {
        return clientInfo != null ? clientInfo : ClientInfo.FALLBACK;
    }

    public void setClientInfo(ClientInfo info) {
        clientInfo = info;
    }

    public record OperationInfo(OperationMode mode, Carriage frontCarriage, Carriage backCarriage) {
        public static final OperationInfo NONE = new OperationInfo(OperationMode.NONE, null, null);
    }

    public enum OperationMode {
        NONE, COUPLING, DECOUPLING;

        public boolean permitted(AllowedOperationMode allowedMode) {
            return this == OperationMode.NONE || allowedMode == AllowedOperationMode.BOTH || (allowedMode == AllowedOperationMode.COUPLING && this == OperationMode.COUPLING) ||
                    (allowedMode == AllowedOperationMode.DECOUPLING && this == OperationMode.DECOUPLING);
        }
    }

    public enum AllowedOperationMode implements StringRepresentable {
        BOTH(true, true),
        COUPLING(true, false),
        DECOUPLING(false, true);

        public final boolean canCouple;
        public final boolean canDecouple;

        AllowedOperationMode(boolean canCouple, boolean canDecouple) {
            this.canCouple = canCouple;
            this.canDecouple = canDecouple;
        }
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public Component getTranslatedName() {
            return Component.translatable("railways.coupler.mode." + getSerializedName());
        }
    }

    public int getTargetAnalogOutput() {
        int out = 0;
        if (getCoupler() != null && getCoupler().isActivated())
            out += 1;

        if (getSecondaryCoupler() != null && getSecondaryCoupler().isActivated())
            out += 2;
        OperationMode mode = getOperationMode();
        if (mode == OperationMode.DECOUPLING)
            out += 4;
        else if (mode == OperationMode.COUPLING)
            out += 8;
        return out;
    }
    protected AABB createRenderBoundingBox() {
        return new AABB(Vec3.atLowerCornerOf(worldPosition), Vec3.atLowerCornerOf(edgePoint.getGlobalPosition()))
                .minmax(new AABB(Vec3.atLowerCornerOf(worldPosition), Vec3.atLowerCornerOf(secondEdgePoint.getGlobalPosition())))
                .inflate(2);
    }
    public void transform(BlockEntity blockEntity, StructureTransform structureTransform) {
        edgePoint.transform(blockEntity, structureTransform);
        secondEdgePoint.transform(blockEntity, structureTransform);
    }

    static class TrackCouplerValueBoxTransform extends CenteredSideValueBoxTransform {

        public TrackCouplerValueBoxTransform(boolean vertical) {
            super((state, d) -> d.getAxis().isVertical() == vertical);
        }
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16);
        }

    }

    public static class ClientInfo {

        public static final ClientInfo FALLBACK = new ClientInfo(OperationMode.NONE, "??", "??", false, Component.literal("??"), Component.literal("??"));

        public OperationMode mode;
        public String trainName1;
        public String trainName2;
        public boolean edgePointsOk;
        public MutableComponent error;
        public MutableComponent error2;

        private ClientInfo(OperationMode mode, String trainName1, String trainName2, boolean edgePointsOk, MutableComponent error, MutableComponent error2) {
            this.mode = mode;
            this.trainName1 = trainName1;
            this.trainName2 = trainName2;
            this.edgePointsOk = edgePointsOk;
            this.error = error;
            this.error2 = error2;
        }

        protected ClientInfo(TrackCouplerBlockEntity te) {
            mode = te.getOperationMode();
            trainName1 = "None";
            trainName2 = "None";
            if (te.getCoupler() != null && te.getCoupler().isActivated()) {
                UUID trainId = te.getCoupler().getCurrentTrain();
                Train train = Create.RAILWAYS.trains.get(trainId);
                if (train != null)
                    trainName1 = train.name.getString();
            }
            if (te.getSecondaryCoupler() != null && te.getSecondaryCoupler().isActivated()) {
                UUID trainId = te.getSecondaryCoupler().getCurrentTrain();
                Train train = Create.RAILWAYS.trains.get(trainId);
                if (train != null)
                    trainName2 = train.name.getString();
            }
            edgePointsOk = te.edgePointsOk;
            error = te.error;
            error2 = te.error2;
        }

        public ClientInfo(CompoundTag tag) {
            mode = NBTHelper.readEnum(tag, "mode", OperationMode.class);
            trainName1 = tag.getString("trainName1").orElse("");
            trainName2 = tag.getString("trainName2").orElse("");
            edgePointsOk = tag.getBoolean("edgePointsOk").orElse(false);
            error = null;
            error2 = null;
        }

        public CompoundTag write() {
            CompoundTag tag = new CompoundTag();
            NBTHelper.writeEnum(tag, "mode", mode);
            tag.putString("trainName1", trainName1);
            tag.putString("trainName2", trainName2);
            tag.putBoolean("edgePointsOk", edgePointsOk);
            return tag;
        }
    }

}
