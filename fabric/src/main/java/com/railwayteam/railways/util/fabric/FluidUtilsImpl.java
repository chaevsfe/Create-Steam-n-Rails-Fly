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

package com.railwayteam.railways.util.fabric;

import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.railwayteam.railways.multiloader.fluid.MultiloaderFluidStack;
import com.railwayteam.railways.shim.create.content.processing.recipe.ProcessingRecipe;
import com.railwayteam.railways.shim.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class FluidUtilsImpl {
    public static boolean canUseAsFuelStorage(BlockEntity be) {
        return be instanceof FuelTankBlockEntity;
    }

    public static Fluid getFluid(Object o) {
        if (o instanceof FluidVariant fluidVariant)
            return fluidVariant.getFluid();
        if (o instanceof FluidStack stack)
            return stack.getFluid();
        if (o instanceof MultiloaderFluidStack stack)
            return stack.getFluid();
        if (o instanceof Fluid fluid)
            return fluid;
        throw new IllegalArgumentException("Expected a fluid stack, fluid variant, or fluid, got: " + o);
    }

    public static void addFluidOutput(ProcessingRecipeBuilder<ProcessingRecipe<?>> b, Fluid fluid, long amount, @Nullable CompoundTag nbt) {
        b.withFluidOutputs(new FluidStack(fluid, (int) Math.min(Integer.MAX_VALUE, amount)));
    }
}
