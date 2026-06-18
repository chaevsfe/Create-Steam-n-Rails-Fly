package com.railwayteam.railways.content.custom_tracks.casing;

import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.AdventureUtils;
import com.railwayteam.railways.util.EntityUtils;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

public class SlabUseOnCurvePacket implements C2SPacket {
    private final BlockPos pos;
    private final BlockPos targetPos;
    private final BlockPos soundSource;

    public SlabUseOnCurvePacket(BlockPos pos, BlockPos targetPos, BlockPos soundSource) {
        this.pos = pos;
        this.targetPos = targetPos;
        this.soundSource = soundSource;
    }

    public SlabUseOnCurvePacket(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        targetPos = buffer.readBlockPos();
        soundSource = buffer.readBlockPos();
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBlockPos(targetPos);
        buffer.writeBlockPos(soundSource);
    }

    public void handle(ServerPlayer player) {
        if (AdventureUtils.isAdventure(player))
            return;
        if (!player.level().isLoaded(pos) || !player.level().isLoaded(targetPos))
            return;
        double reach = EntityUtils.getReachDistance(player) + 1;
        if (player.distanceToSqr(Vec3.atCenterOf(soundSource)) > reach * reach)
            return;
        if (!(player.level().getBlockEntity(pos) instanceof TrackBlockEntity trackBE))
            return;

        BezierConnection connection = trackBE.getConnections().get(targetPos);
        if (connection == null)
            return;
        if (CRTrackMaterials.getType(connection.getMaterial()) == CRTrackMaterials.CRTrackType.MONORAIL)
            return;

        ItemStack handStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        IHasTrackCasing casing = (IHasTrackCasing) connection;

        if (handStack.isEmpty()) {
            Block currentCasing = casing.railways$getTrackCasing();
            if (currentCasing == null)
                return;
            updateCasing(player, trackBE, connection, null, casing.railways$isAlternate());
            if (!player.isCreative())
                EntityUtils.givePlayerItem(player, new ItemStack(currentCasing));
            return;
        }

        if (!(handStack.getItem() instanceof BlockItem blockItem))
            return;

        Block newCasing = blockItem.getBlock();
        if (!CasingChecker.isValid(newCasing))
            return;

        Block currentCasing = casing.railways$getTrackCasing();
        if (currentCasing == newCasing) {
            updateCasing(player, trackBE, connection, newCasing, !casing.railways$isAlternate());
            return;
        }

        if (!player.isCreative()) {
            handStack.shrink(1);
            if (currentCasing != null)
                EntityUtils.givePlayerItem(player, new ItemStack(currentCasing));
        }
        updateCasing(player, trackBE, connection, newCasing, casing.railways$isAlternate());
    }

    private void updateCasing(ServerPlayer player, TrackBlockEntity trackBE, BezierConnection connection,
                              @Nullable Block casing, boolean alternate) {
        setConnectionCasing(connection, casing, alternate);
        trackBE.notifyUpdate();

        if (player.level().getBlockEntity(targetPos) instanceof TrackBlockEntity targetBE) {
            BezierConnection reverse = targetBE.getConnections().get(pos);
            if (reverse != null) {
                setConnectionCasing(reverse, casing, alternate);
                targetBE.notifyUpdate();
            }
        }
    }

    private static void setConnectionCasing(BezierConnection connection, @Nullable Block casing, boolean alternate) {
        IHasTrackCasing hasCasing = (IHasTrackCasing) connection;
        hasCasing.railways$setTrackCasing(casing);
        hasCasing.railways$setAlternate(alternate);
    }
}
