/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.tank;

import com.railwayteam.railways.registry.fabric.CRBlockEntitiesImpl;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Railway's fuel tank keeps Create Fly's native 26.2 multiblock behaviour while
 * using its own block entity type and fuel-only inventory.
 */
public class FuelTankBlock extends FluidTankBlock {
    public FuelTankBlock(Properties properties) {
        super(properties, false);
    }

    public static boolean isTank(BlockState state) {
        return state.getBlock() instanceof FuelTankBlock;
    }

    @Override
    public BlockEntityType<? extends FuelTankBlockEntity> getBlockEntityType() {
        return CRBlockEntitiesImpl.FUEL_TANK.get();
    }
}
