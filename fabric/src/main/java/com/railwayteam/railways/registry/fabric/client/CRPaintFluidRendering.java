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

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.registry.CRFluids;
import com.railwayteam.railways.registry.fabric.CRFluidsImpl;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.content.fluids.VirtualFluid;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class CRPaintFluidRendering {
    private CRPaintFluidRendering() {
    }

    public static void register() {
        VirtualFluid paint = CRFluids.PAINT.get();
        FluidRenderingRegistry.register(
            paint,
            new FluidModel.Unbaked(
                new Material(Railways.asResource("fluid/paint_still/white")),
                new Material(Railways.asResource("fluid/paint_flow/white")),
                null,
                null
            )
        );
        FluidVariantRendering.register(paint, new PaintFluidVariantRenderHandler());

        if (Utils.isDevEnv()) {
            FluidVariant redPaint = FluidVariant.of(
                paint,
                DataComponentPatch.builder()
                    .set(
                        DataComponents.CUSTOM_DATA,
                        CustomData.of(PaintFluid.setColor(new CompoundTag(), PalettesColor.RED))
                    )
                    .build()
            );
            int expectedColor = 0xff000000 | PalettesColor.RED.getDiffuseColor();
            if (FluidVariantRendering.getColor(redPaint) != expectedColor) {
                throw new IllegalStateException("Paint fluid component tint runtime check failed");
            }
            Railways.LOGGER.info("Paint fluid model and component-aware tint runtime check passed");
        }
    }

    private static class PaintFluidVariantRenderHandler implements FluidVariantRenderHandler {
        @Override
        public int getColor(
            FluidVariant variant,
            @Nullable BlockAndTintGetter level,
            @Nullable BlockPos pos
        ) {
            return 0xff000000 | CRFluidsImpl.getPaintColor(variant)
                .orElse(PalettesColor.NETHERITE)
                .getDiffuseColor();
        }
    }
}
