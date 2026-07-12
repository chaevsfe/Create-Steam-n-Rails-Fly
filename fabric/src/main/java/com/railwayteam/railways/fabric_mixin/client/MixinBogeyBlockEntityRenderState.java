/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.coupling.VirtualCouplerRendering.CouplerRenderState;
import com.railwayteam.railways.mixin_interfaces.IBogeyRenderStateVirtualCoupling;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyBlockEntityRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Adds Railway's optional, per-extraction coupler geometry to Create-Fly's bogey render state. */
@Mixin(value = BogeyBlockEntityRenderState.class, remap = false)
public class MixinBogeyBlockEntityRenderState implements IBogeyRenderStateVirtualCoupling {
    @Unique
    private @Nullable CouplerRenderState railways$virtualCouplerRenderState;

    @Override
    public void railways$setVirtualCouplerRenderState(@Nullable CouplerRenderState state) {
        railways$virtualCouplerRenderState = state;
    }

    @Override
    public @Nullable CouplerRenderState railways$getVirtualCouplerRenderState() {
        return railways$virtualCouplerRenderState;
    }
}
