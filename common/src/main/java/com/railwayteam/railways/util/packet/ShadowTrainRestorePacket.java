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

import com.railwayteam.railways.multiloader.S2CPacket;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public record ShadowTrainRestorePacket(Train train) implements S2CPacket {
    public ShadowTrainRestorePacket(FriendlyByteBuf buf) {
        this((Train) null);
    }
    public void write(FriendlyByteBuf buffer) {
        // TODO: Rebuild shadow train restore sync on top of Create Fly's AddTrainPacket codec.
    }
    public void handle(Minecraft mc) {
        mc.execute(() -> {
            // TODO: Restore once TrainRelocator accessors are ported to Create Fly.
        });
    }
}
