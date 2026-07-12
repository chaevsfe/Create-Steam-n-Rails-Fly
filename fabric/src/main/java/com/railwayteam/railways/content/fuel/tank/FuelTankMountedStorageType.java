/*
 * Steam 'n' Rails
 * Copyright (c) 2025-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.tank;

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FuelTankMountedStorageType extends MountedFluidStorageType<FuelTankMountedStorage> {
    public FuelTankMountedStorageType() {
        super(FuelTankMountedStorage.CODEC);
    }

    @Override
    @Nullable
    public FuelTankMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof FuelTankBlockEntity tank && tank.isController()) {
            return FuelTankMountedStorage.fromTank(tank);
        }
        return null;
    }
}
