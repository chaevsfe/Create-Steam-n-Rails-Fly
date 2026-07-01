/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class AddTrainEndPacket implements S2CPacket {
    final UUID trainId;
    final UUID backTrainId;
    final int middleSpacing;
    final boolean doubleEnded;

    public AddTrainEndPacket(Train train, Train backTrain, int middleSpacing, boolean doubleEnded) {
        trainId = train.id;
        this.backTrainId = backTrain.id;
        this.middleSpacing = middleSpacing;
        this.doubleEnded = doubleEnded;
    }

    public AddTrainEndPacket(FriendlyByteBuf buf) {
        trainId = buf.readUUID();
        backTrainId = buf.readUUID();
        middleSpacing = buf.readInt();
        doubleEnded = buf.readBoolean();
    }
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(this.trainId);
        buffer.writeUUID(this.backTrainId);
        buffer.writeInt(this.middleSpacing);
        buffer.writeBoolean(this.doubleEnded);
    }
    public void handle(Minecraft mc) {
        ClientPacketHandlers.handleAddTrainEnd(mc, trainId, backTrainId, middleSpacing, doubleEnded);
    }
}
