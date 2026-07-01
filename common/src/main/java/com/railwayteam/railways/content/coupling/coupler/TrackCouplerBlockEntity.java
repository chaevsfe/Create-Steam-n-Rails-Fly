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
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation;
import com.zurrtum.create.content.trains.signal.SignalBlock;
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

        protected void write(CompoundTag tag, boolean clientPacket) {
        
        tag.putBoolean("EdgePointsOk", edgePointsOk);
        tag.putBoolean("Power", lastReportedPower);
        tag.putInt("AnalogOutput", lastAnalogOutput);
        tag.putInt("EdgeSpacing", edgeSpacing);
        tag.putInt("LastEdgeSpacing", lastEdgeSpacing);
        //if (clientPacket && clientInfo != null)
        //    tag.put("ClientInfo", clientInfo.write());
    }

        protected void read(CompoundTag tag, boolean clientPacket) {
        
        edgePointsOk = tag.getBoolean("EdgePointsOk").orElse(false);
        lastReportedPower = tag.getBoolean("Power").orElse(false);
        lastAnalogOutput = tag.getInt("AnalogOutput").orElse(0);
        edgeSpacing = tag.getInt("EdgeSpacing").orElse(0);
        lastEdgeSpacing = tag.getInt("LastEdgeSpacing").orElse(0);
        edgeSpacingScroll.setValue(edgeSpacing);
        //if (clientPacket)
        //    clientInfo = new ClientInfo(tag.getCompound("ClientInfo").orElse(new CompoundTag()));
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

        blockState.getOptionalValue(SignalBlock.POWERED).ifPresent(powered -> {
            if (lastReportedPower == powered)
                return;
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
        OperationInfo info = getOperationInfo();
        switch (info.mode) {
            case DECOUPLING -> {
                Train train = info.frontCarriage.train;
                int numberOffEnd = train.carriages.size() - train.carriages.indexOf(info.backCarriage); // all carriages after and including the back carriage
                TrainUtils.splitTrain(train, numberOffEnd);
            }
            case COUPLING -> {
                Train frontTrain = info.frontCarriage.train;
                Train backTrain = info.backCarriage.train;
                if (frontTrain == backTrain)
                    break;
                TrainUtils.combineTrains(frontTrain, backTrain, getBlockPos().above(), level, cachedEffectiveEdgeSpacing);
            }
            case NONE -> {
            }
        }
    }

    protected void onUnpowered() {
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
                            ((IOccupiedCouplers) train).railways$getOccupiedCouplers().remove(point.id);
                            if (uuid == point.getCurrentTrain() || train.graph == location.graph) {
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
        return (coupler.isPrimary(relevantPoint.node1) || coupler.isPrimary(relevantPoint.node2) ||
                coupler.isPrimary(relevantPoint2.node1) || coupler.isPrimary(relevantPoint2.node2)) &&
                wheelPosition.distanceToSqr(couplerSpatialPosition) < .8 * .8;
    }

    public AllowedOperationMode getAllowedOperationMode() {
        return getBlockState().getValue(TrackCouplerBlock.MODE);
    }

    public OperationInfo getOperationInfo() {
        clearErrors();
        OperationInfo info = getOperationInfo(false);
        if (info.mode == OperationMode.NONE) {
            MutableComponent backupError = error;
            clearErrors();
            info = getOperationInfo(true);
            if (info.mode == OperationMode.NONE)
                error = backupError;
        }
        if (!info.mode.permitted(getAllowedOperationMode())) {
            clearErrors();
            setError(Component.translatable("railways.tooltip.coupler.error.mode_not_permitted"));
            return OperationInfo.NONE;
        }
        return info;
    }

    protected OperationInfo getOperationInfo(boolean reversed) {
        TrackCoupler coupler1 = reversed ? getSecondaryCoupler() : getCoupler();
        TrackCoupler coupler2 = reversed ? getCoupler() : getSecondaryCoupler();

        TrackTargetingBehaviour<TrackCoupler> edgePoint1 = reversed ? secondEdgePoint : edgePoint;
        TrackTargetingBehaviour<TrackCoupler> edgePoint2 = reversed ? edgePoint : secondEdgePoint;
        if (coupler1 != null && coupler2 != null && coupler1.isActivated() && coupler2.isActivated()) {
            Train primaryTrain = Create.RAILWAYS.trains.get(coupler1.getCurrentTrain());
            Train secondaryTrain = Create.RAILWAYS.trains.get(coupler2.getCurrentTrain());
            if (primaryTrain != null && primaryTrain == secondaryTrain) {
                //Decoupling, if back wheels of a carriage are on the secondary coupler and the front wheels of the carriage behind it are on the primary coupler
                Carriage frontCarriage = getCarriageOnPoint(primaryTrain, coupler2, edgePoint2, false);
                if (frontCarriage == null)
                    setError(Component.translatable("railways.tooltip.coupler.error.carriage_alignment"));
                if (frontCarriage != null && primaryTrain.carriages.indexOf(frontCarriage) < primaryTrain.carriages.size() - 1) {
                    Carriage backCarriage = primaryTrain.carriages.get(primaryTrain.carriages.indexOf(frontCarriage) + 1);
                    if (isCarriageWheelOnPoint(backCarriage, coupler1, edgePoint1, true) &&
                            Math.abs(primaryTrain.carriages.indexOf(frontCarriage) - primaryTrain.carriages.indexOf(backCarriage)) == 1) //Make sure that the carriages are actually next to each other
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
                if (primaryCarriage != null && secondaryCarriage != null && primaryTrain.carriages.indexOf(primaryCarriage) == 0 &&
                        secondaryTrain.carriages.indexOf(secondaryCarriage) == secondaryTrain.carriages.size() - 1) {
                    // ensure correct order when only one bogey (if 'outer' points are closer together than 'inner' points, then something is off
                    double outerLength = secondaryCarriage.getLeadingPoint().getPosition(secondaryTrain.graph)
                        .subtract(primaryCarriage.getTrailingPoint().getPosition(primaryTrain.graph))
                        .lengthSqr();
                    double innerLength = secondaryCarriage.getTrailingPoint().getPosition(secondaryTrain.graph)
                        .subtract(primaryCarriage.getLeadingPoint().getPosition(primaryTrain.graph))
                        .lengthSqr();
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
