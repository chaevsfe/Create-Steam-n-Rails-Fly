package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.content.cycle_menu.TagCycleHandlerServer;
import com.railwayteam.railways.multiloader.C2SPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public class TagCycleSelectionPacket implements C2SPacket {
    private final Item target;

    public TagCycleSelectionPacket(Item target) {
        this.target = target;
    }

    public TagCycleSelectionPacket(FriendlyByteBuf buf) {
        target = BuiltInRegistries.ITEM.getValue(buf.readIdentifier());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeIdentifier(BuiltInRegistries.ITEM.getKey(target));
    }

    public void handle(ServerPlayer sender) {
        TagCycleHandlerServer.select(sender, target);
    }
}
