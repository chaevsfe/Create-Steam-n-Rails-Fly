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

import com.railwayteam.railways.registry.CRBlocks;
import com.zurrtum.create.client.AllBlockTints;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockColors.class)
public class MixinBlockColors {
    @Inject(method = "createDefault()Lnet/minecraft/client/color/block/BlockColors;", at = @At("RETURN"))
    private static void railways$addCopycatTints(CallbackInfoReturnable<BlockColors> cir) {
        BlockColors colors = cir.getReturnValue();
        colors.register(
            List.of(
                new AllBlockTints.WrappedBlockColor(colors, 0),
                new AllBlockTints.WrappedBlockColor(colors, 1),
                new AllBlockTints.WrappedBlockColor(colors, 2)
            ),
            CRBlocks.CONDUCTOR_VENT.get(),
            CRBlocks.COPYCAT_HEADSTOCK.get()
        );
    }
}
