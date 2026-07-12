package com.railwayteam.railways.content.roller_extensions;

import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.contraptions.actors.roller.PaveTask;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrackReplacePaver {
    @ApiStatus.Internal
    public static boolean tickInstantly;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void pave(MovementContext context, BlockPos pos, BlockState stateToPaveWith, @Nullable PaveTask trackProfile) {
        BlockPos trackPos = resolveTrackPos(pos, trackProfile);
        BlockState replacedState = context.world.getBlockState(trackPos);
        if (!(replacedState.getBlock() instanceof ITrackBlock)
            || !(stateToPaveWith.getBlock() instanceof ITrackBlock newTrackBlock)) {
            return;
        }

        FilterItemStack filter = context.getFilterFromBE();
        if (replacedState.getBlock() != stateToPaveWith.getBlock()) {
            BlockState replacementState = copySharedProperties(replacedState, stateToPaveWith);
            ItemStack replacementTrack = extract(filter, context);
            if (!replacementTrack.isEmpty()) {
                TrackData trackData = captureTrackData(context, trackPos, replacedState);
                trackData.detach(context, trackPos);

                boolean previousTickInstantly = tickInstantly;
                boolean replaced;
                try {
                    tickInstantly = true;
                    replaced = context.world.setBlock(trackPos, replacementState, Block.UPDATE_ALL);
                } catch (RuntimeException | Error failure) {
                    try {
                        trackData.restore(context, trackPos);
                    } finally {
                        refund(replacementTrack, filter, context);
                    }
                    throw failure;
                } finally {
                    tickInstantly = previousTickInstantly;
                }

                if (!replaced) {
                    try {
                        trackData.restore(context, trackPos);
                    } finally {
                        refund(replacementTrack, filter, context);
                    }
                } else {
                    trackData.restore(context, trackPos);
                }
            }
        }

        if (!(context.world.getBlockEntity(trackPos) instanceof TrackBlockEntity trackBE)) {
            return;
        }

        boolean changed = false;
        // Work on a snapshot because a neighbour update from a non-standard track implementation may edit the map.
        for (Map.Entry<BlockPos, BezierConnection> entry : new ArrayList<>(trackBE.getConnections().entrySet())) {
            BezierConnection connection = entry.getValue();
            TrackMaterial newMaterial = newTrackBlock.getMaterial();
            if (connection.getMaterial() == newMaterial) {
                continue;
            }

            int requiredTracksForTurn = connection.getTrackItemCost();
            if (extract(filter, context, requiredTracksForTurn).isEmpty()) {
                continue;
            }

            BezierConnection otherConnection = null;
            TrackBlockEntity otherBE = null;
            if (context.world.getBlockEntity(entry.getKey()) instanceof TrackBlockEntity other) {
                otherBE = other;
                otherConnection = other.getConnections().get(trackPos);
            }
            replaceConnectionMaterial(connection, otherConnection, newMaterial);
            if (otherBE != null && otherConnection != null) {
                otherBE.notifyUpdate();
            }
            changed = true;
        }

        if (changed) {
            trackBE.notifyUpdate();
        }
    }

    static BlockPos resolveTrackPos(BlockPos pos, @Nullable PaveTask trackProfile) {
        BlockPos trackPos = pos.above();
        if (trackProfile == null) {
            return trackPos;
        }

        Couple<Integer> key = Couple.create(trackPos.getX(), trackPos.getZ());
        if (!trackProfile.keys().contains(key)) {
            return trackPos;
        }

        int height = (int) trackProfile.get(key);
        return new BlockPos(trackPos.getX(), height + 1, trackPos.getZ());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static BlockState copySharedProperties(BlockState source, BlockState target) {
        for (Property property : source.getProperties()) {
            if (target.hasProperty(property)) {
                target = target.setValue(property, source.getValue(property));
            }
        }
        return target;
    }

    static void replaceConnectionMaterial(BezierConnection connection, @Nullable BezierConnection otherConnection,
                                          TrackMaterial material) {
        connection.setMaterial(material);
        if (otherConnection != null) {
            otherConnection.setMaterial(material);
        }
    }

    static void copyCasing(IHasTrackCasing source, IHasTrackCasing target) {
        target.railways$setTrackCasing(source.railways$getTrackCasing());
        target.railways$setAlternate(source.railways$isAlternate());
    }

    private static TrackData captureTrackData(MovementContext context, BlockPos trackPos, BlockState replacedState) {
        if (!replacedState.getOptionalValue(TrackBlock.HAS_BE).orElse(false)
            || !(context.world.getBlockEntity(trackPos) instanceof TrackBlockEntity trackBE)) {
            return TrackData.EMPTY;
        }

        IHasTrackCasing casing = (IHasTrackCasing) trackBE;
        List<ConnectionData> connections = new ArrayList<>();
        trackBE.getConnections().forEach((otherPos, localConnection) -> {
            BezierConnection remoteConnection = null;
            if (context.world.getBlockEntity(otherPos) instanceof TrackBlockEntity otherBE) {
                remoteConnection = otherBE.getConnections().get(trackPos);
            }
            connections.add(new ConnectionData(otherPos, localConnection, remoteConnection));
        });
        return new TrackData(casing.railways$getTrackCasing(), casing.railways$isAlternate(), connections);
    }

    private static BezierConnection secondaryWithCasing(BezierConnection connection) {
        BezierConnection secondary = connection.secondary();
        copyCasing((IHasTrackCasing) connection, (IHasTrackCasing) secondary);
        return secondary;
    }

    private record ConnectionData(BlockPos otherPos, BezierConnection localConnection,
                                  @Nullable BezierConnection remoteConnection) {
    }

    private record TrackData(@Nullable Block casing, boolean alternate, List<ConnectionData> connections) {
        private static final TrackData EMPTY = new TrackData(null, false, List.of());

        private void detach(MovementContext context, BlockPos trackPos) {
            if (!(context.world.getBlockEntity(trackPos) instanceof TrackBlockEntity trackBE)) {
                return;
            }
            trackBE.getConnections().clear();
            for (ConnectionData connection : connections) {
                if (context.world.getBlockEntity(connection.otherPos()) instanceof TrackBlockEntity otherBE) {
                    otherBE.getConnections().remove(trackPos);
                }
            }
        }

        private void restore(MovementContext context, BlockPos trackPos) {
            if (!(context.world.getBlockEntity(trackPos) instanceof TrackBlockEntity trackBE)) {
                return;
            }

            for (ConnectionData connection : connections) {
                trackBE.getConnections().put(connection.otherPos(), connection.localConnection());
                if (context.world.getBlockEntity(connection.otherPos()) instanceof TrackBlockEntity otherBE) {
                    BezierConnection remote = connection.remoteConnection() != null
                        ? connection.remoteConnection()
                        : secondaryWithCasing(connection.localConnection());
                    otherBE.getConnections().put(trackPos, remote);
                    otherBE.notifyUpdate();
                }
            }

            IHasTrackCasing restoredCasing = (IHasTrackCasing) trackBE;
            restoredCasing.railways$setTrackCasing(casing);
            restoredCasing.railways$setAlternate(alternate);
            trackBE.notifyUpdate();
        }
    }

    public static ItemStack extract(FilterItemStack filter, MovementContext context) {
        return extract(filter, context, 1);
    }

    public static ItemStack extract(FilterItemStack filter, MovementContext context, int amt) {
        return com.railwayteam.railways.content.roller_extensions.fabric.TrackReplacePaverImpl.extract(filter, context, amt);
    }

    private static void refund(ItemStack stack, FilterItemStack filter, MovementContext context) {
        com.railwayteam.railways.content.roller_extensions.fabric.TrackReplacePaverImpl.refund(stack, filter, context);
    }
}
