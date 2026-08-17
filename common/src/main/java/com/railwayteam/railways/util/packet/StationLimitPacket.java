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

import com.railwayteam.railways.mixin_interfaces.ILimited;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StationLimitPacket implements C2SPacket {
    private final BlockPos pos;
    private final boolean limitEnabled;

    public StationLimitPacket(BlockPos pos, boolean limitEnabled) {
        this.pos = pos;
        this.limitEnabled = limitEnabled;
    }

    public StationLimitPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        limitEnabled = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeBoolean(limitEnabled);
    }

    @Override
    public void handle(ServerPlayer sender) {
        Level level = sender.level();

        if (!level.isLoaded(pos))
            return;
        if (!pos.closerThan(sender.blockPosition(), 64))
            return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof StationBlockEntity station))
            return;

        GlobalStation globalStation = station.getStation();
        TrackGraphLocation graphLocation = station.edgePoint.determineGraphLocation();
        if (globalStation == null || graphLocation == null)
            return;

        ((ILimited) globalStation).setLimitEnabled(limitEnabled);
        Create.RAILWAYS.sync.pointAdded(graphLocation.graph, globalStation);
        Create.RAILWAYS.markTracksDirty();
    }
}
