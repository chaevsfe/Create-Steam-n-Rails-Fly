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
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceBlock;
import com.railwayteam.railways.content.fuel.tank.FuelTankBlock;
import com.railwayteam.railways.content.fuel.tank.FuelTankItem;
import com.railwayteam.railways.content.fuel.tank.FuelTankMovementBehavior;
import com.railwayteam.railways.util.CreateBehaviourCompat;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.zurrtum.create.foundation.data.CreateRegistrate;
import com.zurrtum.create.foundation.data.SharedProperties;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/** Fabric-only fuel blocks, loaded from Create Fly's early block callback. */
public final class CRBlocksImpl {
    private static final CreateRegistrate REGISTRATE = Railways.registrate();

    public static final FuelTankMovementBehavior FUEL_TANK_MOVEMENT = new FuelTankMovementBehavior();
    public static final PortableStorageInterfaceMovement PORTABLE_FUEL_INTERFACE_MOVEMENT =
        new PortableStorageInterfaceMovement();

    public static final BlockEntry<FuelTankBlock> FUEL_TANK = REGISTRATE
        .block("fuel_tank", FuelTankBlock::new)
        .initialProperties(SharedProperties::copperMetal)
        .properties(BlockBehaviour.Properties::noOcclusion)
        .properties(properties -> properties.isRedstoneConductor((state, level, pos) -> true))
        .onRegister(CreateBehaviourCompat.movementBehaviour(FUEL_TANK_MOVEMENT))
        .item(FuelTankItem::new)
        .build()
        .register();

    public static final BlockEntry<PortableFuelInterfaceBlock> PORTABLE_FUEL_INTERFACE = REGISTRATE
        .block("portable_fuel_interface", PortableFuelInterfaceBlock::new)
        .initialProperties(SharedProperties::copperMetal)
        .properties(properties -> properties.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
        .onRegister(CreateBehaviourCompat.movementBehaviour(PORTABLE_FUEL_INTERFACE_MOVEMENT))
        .item()
        .build()
        .register();

    private CRBlocksImpl() {
    }

    /** Forces class initialization while vanilla's block registry is still mutable. */
    public static void registerEarly() {
        FUEL_TANK.get();
        PORTABLE_FUEL_INTERFACE.get();
    }

    public static void init() {
        registerEarly();
    }
}
