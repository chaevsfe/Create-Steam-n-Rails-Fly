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

import com.railwayteam.railways.content.custom_tracks.phantom.PhantomTrackOverlays;
import com.zurrtum.create.client.content.trains.track.StandardTrackBlockRenderer;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StandardTrackBlockRenderer.class, remap = false)
public abstract class MixinStandardTrackBlockRenderer {
    @Inject(method = "prepareTrackOverlay", at = @At("HEAD"), cancellable = true)
    private void railways$hideOverlayOnInvisiblePhantom(Affine<?> affine, BlockGetter world, BlockPos pos, BlockState state,
                                                        BezierTrackPointLocation bezierPoint, AxisDirection direction,
                                                        RenderedTrackOverlayType type, CallbackInfo ci) {
        if (PhantomTrackOverlays.isHidden(world, pos, state, bezierPoint)) {
            affine.scale(0);
            ci.cancel();
        }
    }

    @Inject(method = "getRenderState", at = @At("HEAD"), cancellable = true)
    private void railways$skipOverlayOnInvisiblePhantom(Level level, Vec3 offset, BlockState state, BlockPos pos,
                                                        AxisDirection direction, BezierTrackPointLocation bezierPoint,
                                                        RenderedTrackOverlayType type, float scale,
                                                        CallbackInfoReturnable<TrackBlockRenderState> cir) {
        if (PhantomTrackOverlays.isHidden(level, pos, state, bezierPoint))
            cir.setReturnValue(PhantomTrackOverlays.EMPTY);
    }
}
