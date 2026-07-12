package com.railwayteam.railways.util.fabric;

import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.railwayteam.railways.multiloader.fluid.MultiloaderFluidStack;
import com.zurrtum.create.content.processing.recipe.ProcessingRecipe;
import com.zurrtum.create.content.processing.recipe.ProcessingRecipeBuilder;
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
