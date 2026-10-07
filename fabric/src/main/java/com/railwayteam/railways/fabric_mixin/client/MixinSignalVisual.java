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

import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomTrackOverlays;
import com.zurrtum.create.client.content.trains.signal.SignalVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SignalVisual.class, remap = false)
public abstract class MixinSignalVisual extends AbstractBlockEntityVisual<SignalBlockEntity> {
    @Shadow
    private SignalBlockEntity.OverlayState previousOverlayState;

    @Unique
    private Boolean railways$phantomVisible;

    public MixinSignalVisual(VisualizationContext ctx, SignalBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);
    }

    @Inject(method = "setupVisual", at = @At("HEAD"))
    private void railways$refreshOnPhantomVisibilityChange(CallbackInfo ci) {
        boolean visible = PhantomSpriteManager.isVisible();
        if (railways$phantomVisible != null && railways$phantomVisible == visible)
            return;
        railways$phantomVisible = visible;
        if (PhantomTrackOverlays.mayTargetPhantom(blockEntity.getLevel(), blockEntity.edgePoint))
            previousOverlayState = null;
    }
}
