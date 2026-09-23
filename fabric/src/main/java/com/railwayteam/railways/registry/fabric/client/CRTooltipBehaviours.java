/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry.fabric.client;

import com.railwayteam.railways.registry.CRBlockEntities;
import com.railwayteam.railways.registry.fabric.CRBlockEntitiesImpl;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.FluidTankTooltipBehaviour;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class CRTooltipBehaviours {
    private CRTooltipBehaviours() {
    }

    public static void register() {
        AllBlockEntityBehaviours.add(CRBlockEntities.TRACK_COUPLER.get(), CRGoggleTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(CRBlockEntities.ANDESITE_SWITCH.get(), CRGoggleTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(CRBlockEntities.BRASS_SWITCH.get(), CRGoggleTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(CRBlockEntities.SMOKE_STACK.get(), CRGoggleTooltipBehaviour::new);
        AllBlockEntityBehaviours.add(CRBlockEntitiesImpl.FUEL_TANK.get(), FluidTankTooltipBehaviour::new);
    }
}
