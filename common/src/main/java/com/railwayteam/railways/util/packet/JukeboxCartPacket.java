/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.multiloader.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

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
        ClientPacketHandlers.handleJukeboxCart(mc, id, record);
    }
}
