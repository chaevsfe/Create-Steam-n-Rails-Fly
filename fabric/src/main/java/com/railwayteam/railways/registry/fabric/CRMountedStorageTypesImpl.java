/*
 * Steam 'n' Rails
 * Copyright (c) 2025-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.fuel.tank.FuelTankMountedStorageType;
import com.zurrtum.create.AllMountedStorageTypes;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class CRMountedStorageTypesImpl {
    public static final FuelTankMountedStorageType FUEL_TANK = Registry.register(
        CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE,
        ResourceKey.create(CreateRegistryKeys.MOUNTED_FLUID_STORAGE_TYPE, Railways.asResource("fuel_tank")),
        new FuelTankMountedStorageType()
    );

    private static boolean mapped;

    private CRMountedStorageTypesImpl() {
    }

    public static void init() {
        if (mapped) {
            return;
        }
        AllMountedStorageTypes.register(FUEL_TANK, CRBlocksImpl.FUEL_TANK.get());
        mapped = true;
    }
}
