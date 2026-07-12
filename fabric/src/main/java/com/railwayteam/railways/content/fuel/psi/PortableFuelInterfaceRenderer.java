/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.psi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

/** Submit-pipeline renderer for Railway's portable fuel interface partials. */
public class PortableFuelInterfaceRenderer implements BlockEntityRenderer<
    PortableFuelInterfaceBlockEntity,
    PortableFuelInterfaceRenderer.RenderState
> {
    public PortableFuelInterfaceRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(
        PortableFuelInterfaceBlockEntity blockEntity,
        RenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(blockEntity, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.fuelBlockState = blockEntity.getBlockState();
        state.middle = CachedBuffers.partial(
            blockEntity.isConnected()
                ? CRBlockPartials.PORTABLE_FUEL_INTERFACE_MIDDLE_POWERED
                : CRBlockPartials.PORTABLE_FUEL_INTERFACE_MIDDLE,
            state.fuelBlockState
        ).cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.top = CachedBuffers.partial(CRBlockPartials.PORTABLE_FUEL_INTERFACE_TOP, state.fuelBlockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();

        Direction facing = state.fuelBlockState.getValue(PortableFuelInterfaceBlock.FACING);
        state.yRot = KineticBlockEntityRenderer.getYRotateAngle(AngleHelper.horizontalAngle(facing));
        if (facing != Direction.UP) {
            state.xRot = KineticBlockEntityRenderer.getXRotateAngle(facing == Direction.DOWN ? 180 : 90);
        }

        float offset = blockEntity.getExtensionDistance(tickProgress) * 0.5f;
        state.middleOffset = offset + 0.375f;
        state.topOffset = offset - 0.375f;
    }

    @Override
    public void submit(RenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.yRot != null || state.xRot != null) {
            matrices.translate(0.5f, 0.5f, 0.5f);
            if (state.yRot != null) {
                matrices.mulPose(state.yRot);
            }
            if (state.xRot != null) {
                matrices.mulPose(state.xRot);
            }
            matrices.translate(-0.5f, -0.5f, -0.5f);
        }
        matrices.translate(0, state.middleOffset, 0);
        state.middle.submit(matrices, queue);
        matrices.translate(0, state.topOffset, 0);
        state.top.submit(matrices, queue);
    }

    public static class RenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState middle;
        public @UnknownNullability SuperByteBufferRenderState top;
        public @UnknownNullability BlockState fuelBlockState;
        public @Nullable Quaternionf yRot;
        public @Nullable Quaternionf xRot;
        public float middleOffset;
        public float topOffset;
    }
}
