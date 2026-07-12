package com.railwayteam.railways.content.handcar;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.mixin_interfaces.IDeployAnywayBlockItem;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.packet.CurvedTrackHandcarPlacementPacket;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.entity.TravellingPoint.SteerDirection;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.graph.TrackGraphHelper;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem.OverlapResult;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import com.zurrtum.create.infrastructure.packet.s2c.AddTrainPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import static com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity.BOGEY_STYLE_KEY;

public class HandcarItem extends BlockItem implements IDeployAnywayBlockItem {
    public HandcarItem(Block block, Properties properties) {
        super(block, properties);
    }

    private HandcarBlock getBogeyBlock() {
        return (HandcarBlock) getBlock();
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        if (state.getBlock() instanceof ITrackBlock track) {
            var material = track.getMaterial();

            if (!isValidHandcarTrack(material))
                return InteractionResult.FAIL;
            if (level.isClientSide())
                return InteractionResult.SUCCESS;

            Vec3 lookAngle = player.getLookAngle();
            boolean front = track.getNearestTrackAxis(level, pos, state, lookAngle)
                .getSecond() == Direction.AxisDirection.POSITIVE;

            MutableObject<OverlapResult> result = new MutableObject<>(null);
            MutableObject<TrackGraphLocation> resultLoc = new MutableObject<>(null);
            withGraphLocation(level, pos, front, null, (overlap, location) -> {
                result.setValue(overlap);
                resultLoc.setValue(location);
            });

            if (result.getValue() == null)
                return InteractionResult.FAIL;

            if (result.getValue().feedback != null) {
                player.sendOverlayMessage(CreateLang.translateDirect(result.getValue().feedback)
                    .withStyle(ChatFormatting.RED));
                AllSoundEvents.DENY.play(level, null, pos, .5f, 1);
                return InteractionResult.FAIL;
            }

            TrackGraphLocation loc = resultLoc.getValue();
            if (loc == null)
                return InteractionResult.FAIL;

            boolean success = placeHandcar(loc, level, player, pos);
            if (success && !player.isCreative())
                stack.shrink(1);
            return success ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    public boolean placeHandcar(TrackGraphLocation trackGraphLocation, Level level, Player player, BlockPos soundPos) {
        TrackGraph graph = trackGraphLocation.graph;
        TrackNode node1 = graph.locateNode(trackGraphLocation.edge.getFirst());
        TrackNode node2 = graph.locateNode(trackGraphLocation.edge.getSecond());

        if (node1 == null || node2 == null)
            return false;

        TrackEdge edge = graph.getConnectionsFrom(node1).get(node2);
        if (edge == null)
            return false;

        double offset = getBogeyBlock().getWheelPointSpacing() / 2;
        TravellingPoint tp1 = new TravellingPoint(node1, node2, edge, trackGraphLocation.position, false);
        TravellingPoint tp2 = new TravellingPoint(node1, node2, edge, trackGraphLocation.position, false);
        tp1.travel(graph, offset, tp1.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));
        tp2.travel(graph, -offset, tp2.steer(SteerDirection.NONE, new Vec3(0, 1, 0)));

        if (!(level instanceof ServerLevel serverLevel))
            return false;

        Train train = makeTrain(player.getUUID(), graph, tp1, tp2, serverLevel);
        if (train == null)
            return false;

        AllSoundEvents.CONTROLLER_CLICK.play(level, null, soundPos, 1, 1);
        return true;
    }

    @Environment(EnvType.CLIENT)
    public boolean useOnCurve(TrackBlockOutline.BezierPointSelection selection, ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null)
            return false;

        BezierConnection bc = selection.blockEntity().getConnections().get(selection.loc().curveTarget());
        if (bc == null)
            return false;
        var material = bc.getMaterial();
        if (!isValidHandcarTrack(material))
            return false;

        boolean front = player.getLookAngle()
            .dot(selection.direction()) < 0;

        CRPackets.PACKETS.send(new CurvedTrackHandcarPlacementPacket(selection.blockEntity().getBlockPos(),
            selection.loc().curveTarget(), selection.loc().segment(), front, player.getInventory().getSelectedSlot()));
        return true;
    }

    private @Nullable Train makeTrain(UUID owner, TrackGraph graph, TravellingPoint tp1, TravellingPoint tp2,
                                      ServerLevel level) {
        CarriageContraption contraption = new CarriageContraption(Direction.EAST);

        SchematicLevel assemblyWorld = new SchematicLevel(level);
        StructureTemplate template = level.getStructureManager()
            .get(Railways.asResource("handcar/assembly"))
            .orElse(null);
        if (template == null)
            return null;

        StructurePlaceSettings settings = new StructurePlaceSettings();
        template.placeInWorld(assemblyWorld, BlockPos.ZERO, BlockPos.ZERO, settings, level.getRandom(), Block.UPDATE_CLIENTS);

        try {
            contraption.assemble(assemblyWorld, new BlockPos(1, 1, 1));
        } catch (AssemblyException e) {
            return null;
        }

        contraption.expandBoundsAroundAxis(Axis.Y);

        CompoundTag bogeyData = new CompoundTag();
        bogeyData.store(BOGEY_STYLE_KEY, Identifier.CODEC, CRBogeyStyles.HANDCAR.id);
        CarriageBogey bogey = new CarriageBogey(getBogeyBlock(), false, bogeyData, tp1, tp2);
        Carriage carriage = new Carriage(bogey, null, 0);
        Train train = new Train(UUID.randomUUID(), owner, graph, List.of(carriage), new ArrayList<>(), true, 0);

        ((IHandcarTrain) train).railways$setHandcar(true);

        carriage.setContraption(level, contraption);
        train.name = Component.translatable("block.railways.handcar");
        train.collectInitiallyOccupiedSignalBlocks();
        Create.RAILWAYS.addTrain(train);
        level.getServer().getPlayerList().broadcastAll(new AddTrainPacket(train));
        return train;
    }

    public static void withGraphLocation(Level level, BlockPos pos, boolean front,
                                         BezierTrackPointLocation targetBezier,
                                         BiConsumer<OverlapResult, TrackGraphLocation> callback) {
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof ITrackBlock track)) {
            callback.accept(OverlapResult.NO_TRACK, null);
            return;
        }

        List<Vec3> trackAxes = track.getTrackAxes(level, pos, state);
        if (targetBezier == null && trackAxes.size() > 1) {
            callback.accept(OverlapResult.JUNCTION, null);
            return;
        }

        Direction.AxisDirection targetDirection =
            front ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
        TrackGraphLocation location = targetBezier != null
            ? TrackGraphHelper.getBezierGraphLocationAt(level, pos, targetDirection, targetBezier)
            : TrackGraphHelper.getGraphLocationAt(level, pos, targetDirection, trackAxes.get(0));

        if (location == null) {
            callback.accept(OverlapResult.NO_TRACK, null);
            return;
        }

        Couple<TrackNode> nodes = location.edge.map(location.graph::locateNode);
        TrackEdge edge = location.graph.getConnection(nodes);
        if (edge == null) {
            callback.accept(OverlapResult.NO_TRACK, null);
            return;
        }

        callback.accept(OverlapResult.VALID, location);
    }

    public static boolean isValidHandcarTrack(com.zurrtum.create.content.trains.track.TrackMaterial material) {
        return CRTrackMaterials.getType(material) == CRTrackMaterials.CRTrackType.STANDARD
            || CRTrackMaterials.getType(material) == CRTrackMaterials.CRTrackType.UNIVERSAL;
    }
}
