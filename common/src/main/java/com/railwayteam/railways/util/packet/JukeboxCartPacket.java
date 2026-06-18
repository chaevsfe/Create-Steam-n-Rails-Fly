package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.content.minecarts.MinecartJukebox;
import com.railwayteam.railways.multiloader.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class JukeboxCartPacket implements S2CPacket {
    private final int id;
    private final ItemStack record;

    public JukeboxCartPacket(Entity target, ItemStack disc) {
        id = target.getId();
        record = disc.copy();
    }

    public JukeboxCartPacket(FriendlyByteBuf buf) {
        id = buf.readInt();
        record = buf.readWithCodecTrusted(NbtOps.INSTANCE, ItemStack.OPTIONAL_CODEC);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.id);
        buffer.writeWithCodec(NbtOps.INSTANCE, ItemStack.OPTIONAL_CODEC, this.record);
    }

    public void handle(Minecraft mc) {
        Level level = mc.level;
        if (level == null)
            return;

        Entity target = level.getEntity(this.id);
        if (target instanceof MinecartJukebox juke)
            juke.insertRecord(this.record);
    }
}
