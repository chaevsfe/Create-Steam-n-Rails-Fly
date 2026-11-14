/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.railwayteam.railways.annotation.mixin.ConditionalMixin;
import com.railwayteam.railways.compat.Mods;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@ConditionalMixin(mods = Mods.IRIS)
@Mixin(RenderSetup.RenderSetupBuilder.class)
public class MixinRenderSetupBuilder {
    @Unique
    private static final Set<Identifier> railways$ENTITY_BLOCK_PIPELINES = Set.of(
        Identifier.fromNamespaceAndPath("ponder", "pipeline/entity_block_solid"),
        Identifier.fromNamespaceAndPath("ponder", "pipeline/entity_block_cutout"),
        Identifier.fromNamespaceAndPath("ponder", "pipeline/entity_block_translucent"),
        Identifier.fromNamespaceAndPath("ponder", "pipeline/nether_entity_block_solid"),
        Identifier.fromNamespaceAndPath("ponder", "pipeline/nether_entity_block_cutout"),
        Identifier.fromNamespaceAndPath("ponder", "pipeline/nether_entity_block_translucent")
    );

    @Shadow
    @Final
    private RenderPipeline pipeline;

    @Shadow
    private boolean useOverlay;

    @Inject(method = "createRenderSetup", at = @At("HEAD"))
    private void railways$bindOverlayForPonderEntityBlocks(CallbackInfoReturnable<RenderSetup> cir) {
        if (!useOverlay && railways$ENTITY_BLOCK_PIPELINES.contains(pipeline.getLocation()))
            useOverlay = true;
    }
}
