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
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public class OverridableSignalPacket implements S2CPacket {

    final BlockPos blockPos;
    @Nullable
    final BlockPos signalPos;
    final SignalBlockEntity.SignalState signalState;
    final int ticks;
    final boolean distantSignal;

    public OverridableSignalPacket(BlockPos displayPos, @Nullable BlockPos signalPos,
                                   SignalBlockEntity.SignalState signalState, int ticks, boolean distantSignal) {
        blockPos = displayPos;
        this.signalPos = signalPos;
        this.signalState = signalState;
        this.ticks = ticks;
        this.distantSignal = distantSignal;
    }

    public OverridableSignalPacket(FriendlyByteBuf buf) {
        blockPos = buf.readBlockPos();
        if (buf.readBoolean()) {
            signalPos = buf.readBlockPos();
        } else {
            signalPos = null;
        }
        signalState = SignalBlockEntity.SignalState.values()[buf.readInt()];
        ticks = buf.readInt();
        distantSignal = buf.readBoolean();
    }
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos);
        buffer.writeBoolean(signalPos != null);
        if (signalPos != null)
            buffer.writeBlockPos(signalPos);
        buffer.writeInt(signalState.ordinal());
        buffer.writeInt(ticks);
        buffer.writeBoolean(distantSignal);
    }
    public void handle(Minecraft mc) {
        ClientPacketHandlers.handleOverridableSignal(mc, blockPos, signalPos, signalState, ticks, distantSignal);
    }
}
