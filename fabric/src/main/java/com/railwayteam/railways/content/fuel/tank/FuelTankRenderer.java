/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.tank;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** Railway's tank uses Create Fly's 26.2 render-state extraction and submit pipeline. */
public class FuelTankRenderer extends com.zurrtum.create.client.content.fluids.tank.FluidTankRenderer {
    public FuelTankRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
