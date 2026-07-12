/*
 * Steam 'n' Rails
 * Copyright (c) 2025-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.google.common.collect.ImmutableMap;
import com.railwayteam.railways.mixin_interfaces.IFuelInventory;
import com.railwayteam.railways.util.AbstractionUtils;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.content.contraptions.MountedStorageManager;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MountedStorageManager.class, remap = false)
public abstract class MixinMountedStorageManagerFuel implements IFuelInventory {
    @Shadow
    protected MountedFluidStorageWrapper fluids;

    @Unique
    private @Nullable MountedFluidStorageWrapper railways$fluidFuels;

    @Inject(method = "initialize", at = @At("TAIL"))
    private void railways$initializeFluidFuels(CallbackInfo ci) {
        ImmutableMap.Builder<BlockPos, MountedFluidStorage> builder = ImmutableMap.builder();
        fluids.storages.forEach((pos, storage) -> {
            if (AbstractionUtils.isInstanceOfFuelTankMountedStorageType(storage.type)) {
                builder.put(pos, storage);
            }
        });
        ImmutableMap<BlockPos, MountedFluidStorage> fuelStorages = builder.build();
        railways$fluidFuels = fuelStorages.isEmpty() ? null : new MountedFluidStorageWrapper(fuelStorages);
    }

    @Override
    public MountedFluidStorageWrapper railways$getFluidFuels() {
        return railways$fluidFuels;
    }
}
