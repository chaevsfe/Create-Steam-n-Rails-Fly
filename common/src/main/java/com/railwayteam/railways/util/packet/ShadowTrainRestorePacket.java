/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.railwayteam.railways.multiloader.S2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/** Marks the following Create AddTrainPacket as a relocation-only shadow train snapshot. */
public record ShadowTrainRestorePacket(UUID trainId) implements S2CPacket {
    public ShadowTrainRestorePacket(FriendlyByteBuf buf) {
        this(UUIDUtil.STREAM_CODEC.decode(buf));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        UUIDUtil.STREAM_CODEC.encode(buffer, trainId);
    }

    @Override
    public void handle(Minecraft mc) {
        // PacketSet already dispatches handle() onto the client thread. Do not enqueue this again:
        // the native AddTrainPacket sent immediately afterwards must observe the marker first.
        ShadowRealm.clientPendingShadowTrainId = trainId;
    }
}
