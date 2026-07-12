/*
 * Steam 'n' Rails
 * Copyright (c) 2024-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.railwayteam.railways.base.datafixerapi.DataFixesInternals;
import net.minecraft.util.datafix.DataFixTypes;
import org.spongepowered.asm.mixin.Mixin;

/** Runs the isolated Railways fixer after vanilla has updated a persisted root object. */
@Mixin(DataFixTypes.class)
public abstract class MixinDataFixTypes {
    @WrapMethod(
        method = "update(Lcom/mojang/datafixers/DataFixer;Lcom/mojang/serialization/Dynamic;II)Lcom/mojang/serialization/Dynamic;"
    )
    private <T> Dynamic<T> railways$updateWithModFixers(
        DataFixer fixer,
        Dynamic<T> input,
        int version,
        int newVersion,
        Operation<Dynamic<T>> original
    ) {
        Dynamic<T> vanillaFixed = original.call(fixer, input, version, newVersion);
        return DataFixesInternals.get().updateWithAllFixers((DataFixTypes) (Object) this, vanillaFixed);
    }
}
