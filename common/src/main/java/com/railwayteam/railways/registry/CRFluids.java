/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.registry;

import com.zurrtum.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public class CRFluids {
    public static final FluidEntry<VirtualFluid> PAINT = registerPaint();

    public static void register() {}

    private static FluidEntry<VirtualFluid> registerPaint() {
        return com.railwayteam.railways.registry.fabric.CRFluidsImpl.registerPaint();
    }

    @Environment(EnvType.CLIENT)
    public static void initRendering() {
        com.railwayteam.railways.registry.fabric.CRFluidsImpl.initRendering();
    }
}
