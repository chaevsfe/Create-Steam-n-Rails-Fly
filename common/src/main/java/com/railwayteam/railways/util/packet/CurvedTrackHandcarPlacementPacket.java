package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.content.handcar.HandcarItem;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.commons.lang3.mutable.MutableObject;

public class CurvedTrackHandcarPlacementPacket implements C2SPacket {
    private final BlockPos pos;
    private final BlockPos targetPos;
    private final int segment;
    private final boolean front;
    private final int slot;

    public CurvedTrackHandcarPlacementPacket(BlockPos pos, BlockPos targetPos, int segment, boolean front, int slot) {
        this.pos = pos;
        this.targetPos = targetPos;
        this.segment = segment;
        this.front = front;
        this.slot = slot;
    }

    public CurvedTrackHandcarPlacementPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        targetPos = buf.readBlockPos();
        segment = buf.readVarInt();
        front = buf.readBoolean();
        slot = buf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBlockPos(targetPos);
        buffer.writeVarInt(segment);
        buffer.writeBoolean(front);
        buffer.writeVarInt(slot);
    }

    private void handle(ServerPlayer player, TrackBlockEntity be) {
        if (player.getInventory().getSelectedSlot() != slot)
            return;

        ItemStack stack = player.getInventory().getItem(slot);
        if (!(stack.getItem() instanceof HandcarItem handcarItem))
            return;

        BezierConnection bc = be.getConnections().get(targetPos);
        if (bc == null)
            return;
        var material = bc.getMaterial();
        if (!HandcarItem.isValidHandcarTrack(material))
            return;

        MutableObject<TrackTargetingBlockItem.OverlapResult> result = new MutableObject<>(null);
        MutableObject<TrackGraphLocation> resultLoc = new MutableObject<>(null);
        HandcarItem.withGraphLocation(player.level(), pos, front,
            new BezierTrackPointLocation(targetPos, segment), (overlap, location) -> {
                result.setValue(overlap);
                resultLoc.setValue(location);
            });

        if (result.getValue() == null)
            return;

        if (result.getValue().feedback != null) {
            player.displayClientMessage(CreateLang.translateDirect(result.getValue().feedback)
                .withStyle(ChatFormatting.RED), true);
            AllSoundEvents.DENY.play(player.level(), null, pos, .5f, 1);
            return;
        }

        TrackGraphLocation loc = resultLoc.getValue();
        if (loc == null)
            return;

        boolean success = handcarItem.placeHandcar(loc, player.level(), player, pos);
        if (success && !player.isCreative())
            stack.shrink(1);
    }

    @Override
    public void handle(ServerPlayer sender) {
        Level level = sender.level();

        if (!level.isLoaded(pos))
            return;
        if (!pos.closerThan(sender.blockPosition(), 64))
            return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TrackBlockEntity trackBlockEntity) {
            handle(sender, trackBlockEntity);
            return;
        }
    }
}
