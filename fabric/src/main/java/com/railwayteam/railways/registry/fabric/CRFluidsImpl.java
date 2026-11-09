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

package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.shim.create.content.fluids.VirtualFluid;
import com.railwayteam.railways.shim.registrate.util.entry.FluidEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public class CRFluidsImpl {
    public static FluidEntry<VirtualFluid> registerPaint() {
        VirtualFluid paint = Registry.register(BuiltInRegistries.FLUID, Railways.asResource("paint"), new VirtualFluid());
        FluidVariantAttributes.register(paint, new PaintFluidVariantAttributeHandler());
        return new FluidEntry<>(Railways.asResource("paint"), paint);
    }

    @Environment(EnvType.CLIENT)
    public static void initRendering() {
        com.railwayteam.railways.registry.fabric.client.CRPaintFluidRendering.register();
    }

    public static Optional<PalettesColor> getPaintColor(FluidVariant variant) {
        CustomData data = variant.get(DataComponents.CUSTOM_DATA);
        return data == null ? Optional.empty() : PaintFluid.getColor(data.copyTag());
    }

    private static class PaintFluidVariantAttributeHandler implements FluidVariantAttributeHandler {
        @Override
        public Component getName(FluidVariant variant) {
            return Component.translatable(getPaintColor(variant)
                .map(PalettesColor::getPaintNameId)
                .orElse("fluid.railways.paint"));
        }
    }
}
