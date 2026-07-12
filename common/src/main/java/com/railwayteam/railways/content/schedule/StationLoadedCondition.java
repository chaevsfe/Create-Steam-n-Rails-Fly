/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.schedule;

import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.zurrtum.create.content.trains.station.GlobalStation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/** Completes once the station's block-entity chunk is ticking. */
public class StationLoadedCondition extends ScheduleWaitCondition {
    public StationLoadedCondition(Identifier id) {
        super(id);
    }

    @Override
    public boolean tickCompletion(Level level, Train train, CompoundTag context) {
        GlobalStation currentStation = train.getCurrentStation();
        if (currentStation == null) {
            return false;
        }

        ResourceKey<Level> stationDimension = currentStation.getBlockEntityDimension();
        MinecraftServer server = level.getServer();
        if (server == null) {
            return false;
        }

        ServerLevel stationLevel = server.getLevel(stationDimension);
        return stationLevel != null && stationLevel.isPositionEntityTicking(currentStation.getBlockEntityPos());
    }

    @Override
    public MutableComponent getWaitingStatus(Level level, Train train, CompoundTag context) {
        return Component.translatable("railways.schedule.condition.loaded.status");
    }
}
