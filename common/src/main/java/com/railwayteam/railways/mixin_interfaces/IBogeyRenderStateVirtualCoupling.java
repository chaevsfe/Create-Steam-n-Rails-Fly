/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.mixin_interfaces;

import com.railwayteam.railways.content.coupling.VirtualCouplerRendering.CouplerRenderState;
import org.jetbrains.annotations.Nullable;

/** Client render-state bridge used by the Create-Fly 26.2 bogey renderer mixins. */
public interface IBogeyRenderStateVirtualCoupling {
    void railways$setVirtualCouplerRenderState(@Nullable CouplerRenderState state);

    @Nullable
    CouplerRenderState railways$getVirtualCouplerRenderState();
}
