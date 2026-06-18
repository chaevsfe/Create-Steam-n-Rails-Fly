package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.multiloader.C2SPacket;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class BogeyStyleSelectionPacket implements C2SPacket {
    public BogeyStyleSelectionPacket(BogeyStyle style) {
    }

    public BogeyStyleSelectionPacket(BogeyStyle style, @Nullable BogeySize size) {
    }

    public BogeyStyleSelectionPacket(FriendlyByteBuf buf) {
    }

    public void write(FriendlyByteBuf buffer) {
    }

    public void handle(ServerPlayer sender) {
    }
}
