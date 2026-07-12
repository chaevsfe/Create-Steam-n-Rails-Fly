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

import com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** A Create Fly fluid tank whose insertion rule is Railway's liquid-fuel rule. */
public class FuelTankBlockEntity extends FluidTankBlockEntity {
    public FuelTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected FluidTank createInventory() {
        return new FuelTankInventory(getCapacityMultiplier());
    }

    public class FuelTankInventory
        extends com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity.FluidTankInventory {
        public FuelTankInventory(int capacity) {
            super(capacity);
        }

        @Override
        public boolean isValid(int slot, FluidStack stack) {
            return LiquidFuelTrainHandler.isFuelForTanks(stack.getFluid());
        }
    }
}
