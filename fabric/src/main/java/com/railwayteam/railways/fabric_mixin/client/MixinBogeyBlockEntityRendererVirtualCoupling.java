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

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.coupling.VirtualCouplerRendering;
import com.railwayteam.railways.content.coupling.VirtualCouplerRendering.CouplerRenderState;
import com.railwayteam.railways.mixin_interfaces.IBogeyRenderStateVirtualCoupling;
import com.railwayteam.railways.mixin_interfaces.IStandardBogeyTEVirtualCoupling;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyBlockEntityRenderState;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.bogey.StandardBogeyBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Bridges Ponder's transient bogey coupling values into Create-Fly 26.2's render-state pipeline. */
@Mixin(value = BogeyBlockEntityRenderer.class, remap = false)
public class MixinBogeyBlockEntityRendererVirtualCoupling {
    @Inject(
        method = "extractRenderState(Lcom/zurrtum/create/content/trains/bogey/AbstractBogeyBlockEntity;Lcom/zurrtum/create/client/content/trains/bogey/BogeyBlockEntityRenderer$BogeyBlockEntityRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At("TAIL")
    )
    private void railways$extractVirtualCoupler(
        AbstractBogeyBlockEntity blockEntity,
        BogeyBlockEntityRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        CrumblingOverlay crumblingOverlay,
        CallbackInfo ci
    ) {
        IBogeyRenderStateVirtualCoupling stateExtension = (IBogeyRenderStateVirtualCoupling) state;
        // Render-state instances are reused. Always clear first so a regular bogey cannot inherit
        // geometry extracted for a previous Ponder bogey.
        stateExtension.railways$setVirtualCouplerRenderState(null);

        if (!(blockEntity instanceof StandardBogeyBlockEntity)
            || !(blockEntity instanceof IStandardBogeyTEVirtualCoupling virtualCoupling))
            return;

        double distance = virtualCoupling.getCouplingDistance();
        Direction direction = virtualCoupling.getCouplingDirection();
        if (!Double.isFinite(distance) || distance <= 0 || direction == null)
            return;

        CouplerRenderState couplerState = VirtualCouplerRendering.createRenderState(
            direction,
            distance,
            virtualCoupling.getFront(),
            state.lightCoords,
            blockEntity.getBlockState()
        );
        stateExtension.railways$setVirtualCouplerRenderState(couplerState);
    }

    @Inject(
        method = "submit(Lcom/zurrtum/create/client/content/trains/bogey/BogeyBlockEntityRenderer$BogeyBlockEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("RETURN")
    )
    private void railways$submitVirtualCoupler(
        BogeyBlockEntityRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState,
        CallbackInfo ci
    ) {
        CouplerRenderState couplerState =
            ((IBogeyRenderStateVirtualCoupling) state).railways$getVirtualCouplerRenderState();
        if (couplerState != null)
            couplerState.submit(matrices, queue);
    }
}
