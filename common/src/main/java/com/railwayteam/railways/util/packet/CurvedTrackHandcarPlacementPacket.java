package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.multiloader.C2SPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class CurvedTrackHandcarPlacementPacket implements C2SPacket {
    public CurvedTrackHandcarPlacementPacket(BlockPos pos, BlockPos targetPos, int segment, boolean front, int slot) {
    }

    public CurvedTrackHandcarPlacementPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buffer) {
    }

    public void handle(ServerPlayer sender) {
    }
}
