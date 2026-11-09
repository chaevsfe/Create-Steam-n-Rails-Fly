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

package com.railwayteam.railways.content.fuel.fabric;

import com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jetbrains.annotations.Nullable;

public final class LiquidFuelTrainHandlerImpl {
    private static final int FUEL_DRAIN_AMOUNT = 8_100;

    private LiquidFuelTrainHandlerImpl() {
    }

    public static int handleFuelDraining(MountedFluidStorageWrapper fluidFuels) {
        return handleFuelDraining(fluidFuels, null);
    }

    public static int handleFuelDraining(
        MountedFluidStorageWrapper fluidFuels,
        @Nullable FuelValues fuelValues
    ) {
        for (int slot = 0; slot < fluidFuels.size(); slot++) {
            FluidStack stack = fluidFuels.getStack(slot);
            if (stack.isEmpty() || stack.getAmount() < FUEL_DRAIN_AMOUNT) {
                continue;
            }

            int burnTime = LiquidFuelTrainHandler.handleFuelChecking(stack, fuelValues);
            if (burnTime <= 0) {
                continue;
            }

            if (fluidFuels.extract(stack, FUEL_DRAIN_AMOUNT) == FUEL_DRAIN_AMOUNT) {
                return burnTime;
            }
        }
        return 0;
    }
}
