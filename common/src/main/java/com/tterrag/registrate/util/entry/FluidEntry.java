package com.tterrag.registrate.util.entry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public class FluidEntry<T extends Fluid> extends RegistryEntry<T> {
    public FluidEntry(Identifier id, T value) {
        super(id, value);
    }
}
