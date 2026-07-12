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

import com.railwayteam.railways.registry.CRSpriteShifts;
import com.zurrtum.create.client.content.fluids.tank.FluidTankCTBehaviour;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel.UnbakedRoot;
import net.minecraft.world.level.block.state.BlockState;

/** Create Fly 26.2 block-state model using Railway's connected-texture sprites. */
public class FuelTankModel extends com.zurrtum.create.client.infrastructure.model.FluidTankModel {
    public FuelTankModel(BlockState state, UnbakedRoot unbaked) {
        super(
            state,
            unbaked,
            new FluidTankCTBehaviour(
                CRSpriteShifts.FUEL_TANK,
                CRSpriteShifts.FUEL_TANK_TOP,
                CRSpriteShifts.FUEL_TANK_INNER
            )
        );
    }

    public static FuelTankModel standard(BlockState state, UnbakedRoot unbaked) {
        return new FuelTankModel(state, unbaked);
    }
}
