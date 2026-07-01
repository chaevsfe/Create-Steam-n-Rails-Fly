package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.zurrtum.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class CRFluidsImpl {
    public static FluidEntry<VirtualFluid> registerPaint() {
        VirtualFluid paint = Registry.register(BuiltInRegistries.FLUID, Railways.asResource("paint"), new VirtualFluid());
        return new FluidEntry<>(Railways.asResource("paint"), paint);
    }

    public static void initRendering() {
    }
}
