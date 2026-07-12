/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlockEntity;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

public final class CRBlockEntitiesImpl {
    public static final BlockEntityEntry<FuelTankBlockEntity> FUEL_TANK = Railways.registrate()
        .blockEntity("fuel_tank", FuelTankBlockEntity::new)
        .validBlocks(CRBlocksImpl.FUEL_TANK)
        .register();

    public static final BlockEntityEntry<PortableFuelInterfaceBlockEntity> PORTABLE_FUEL_INTERFACE =
        Railways.registrate()
            .blockEntity("portable_fuel_interface", PortableFuelInterfaceBlockEntity::new)
            .validBlocks(CRBlocksImpl.PORTABLE_FUEL_INTERFACE)
            .register();

    private static boolean transferRegistered;

    private CRBlockEntitiesImpl() {
    }

    public static void init() {
        if (transferRegistered) {
            return;
        }

        registerFluidSide(FUEL_TANK.get(), blockEntity -> {
            if (blockEntity.fluidCapability == null) {
                blockEntity.refreshCapability();
            }
            return blockEntity.fluidCapability;
        });
        registerFluidSide(PORTABLE_FUEL_INTERFACE.get(), blockEntity -> blockEntity.capability);
        transferRegistered = true;
    }

    private static <T extends SmartBlockEntity> void registerFluidSide(
        BlockEntityType<T> type,
        Function<T, FluidInventory> inventory
    ) {
        BlockEntityBehaviour.add(type, blockEntity -> new CachedFluidInventoryBehaviour<>(blockEntity, inventory));
        FluidStorage.SIDED.registerForBlockEntity(CachedFluidInventoryBehaviour::get, type);
    }
}
