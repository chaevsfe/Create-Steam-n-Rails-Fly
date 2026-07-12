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
