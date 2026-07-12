/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceRenderer;
import com.railwayteam.railways.content.fuel.psi.PortableFuelInterfaceMovementRender;
import com.railwayteam.railways.content.fuel.tank.FuelTankModel;
import com.railwayteam.railways.content.fuel.tank.FuelTankRenderer;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllModels;

/** Client-only fuel model and block-entity renderer wiring. */
public final class CRBlockEntitiesClientImpl {
    private CRBlockEntitiesClientImpl() {
    }

    public static void register() {
        AllModels.register(CRBlocksImpl.FUEL_TANK.get(), FuelTankModel::standard);
        CRBlocksImpl.PORTABLE_FUEL_INTERFACE_MOVEMENT.attachRender = new PortableFuelInterfaceMovementRender();
        AllBlockEntityRenders.render(CRBlockEntitiesImpl.FUEL_TANK.get(), FuelTankRenderer::new);
        AllBlockEntityRenders.render(
            CRBlockEntitiesImpl.PORTABLE_FUEL_INTERFACE.get(),
            PortableFuelInterfaceRenderer::new
        );
    }
}
