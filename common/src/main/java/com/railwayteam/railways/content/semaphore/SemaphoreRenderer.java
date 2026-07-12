/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.content.semaphore;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.catnip.math.AngleHelper;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SemaphoreRenderer extends SmartBlockEntityRenderer<SemaphoreBlockEntity, SemaphoreRenderer.SemaphoreRenderState> {

    public SemaphoreRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SemaphoreRenderState createRenderState() {
        return new SemaphoreRenderState();
    }

    @Override
    public void extractRenderState(SemaphoreBlockEntity be, SemaphoreRenderState state,
                                   float tickProgress, Vec3 cameraPos,
                                   @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumbling);
        if (be.isRemoved()) return;

        BlockState blockState = be.getBlockState();
        state.blockState = blockState;
        state.yRot = AngleHelper.horizontalAngle(blockState.getValue(SemaphoreBlock.FACING)) + 180;

        boolean yellow = be.isDistantSignal;

        float pos = be.armPosition.getValue(tickProgress);
        float target = be.armPosition.getChaseTarget();

        // Animation easing: flip pos depending on which direction the arm is moving
        pos = (2 * pos - 1) * (target - 0.5f) + 0.5f;
        float fallTime = 0.3f;
        if (pos < fallTime) {
            pos = 1f - pos * pos / (fallTime * fallTime);
        } else {
            pos = (pos - fallTime) / (1f - fallTime);
            float bounce = (float) (Math.exp(-pos * 4.0) * Math.sin(pos * Math.PI * 3.0));
            float smoothing = 0.1f;
            bounce = (float) Math.sqrt(bounce * bounce + smoothing * smoothing) - smoothing;
            pos = bounce / 3f;
        }
        pos = -(2 * pos - 1) * (target - 0.5f) + 0.5f;

        boolean flipped = blockState.getValue(SemaphoreBlock.FLIPPED);
        boolean upside_down = blockState.getValue(SemaphoreBlock.UPSIDE_DOWN);

        // armAngle is in radians; sign flips for upside-down mounting
        state.armAngle = pos * 0.78f * (upside_down ? -1 : 1);

        PartialModel arm;
        if (upside_down) {
            arm = flipped
                ? (yellow ? CRBlockPartials.SEMAPHORE_ARM_YELLOW_FLIPPED_UPSIDE_DOWN : CRBlockPartials.SEMAPHORE_ARM_RED_FLIPPED_UPSIDE_DOWN)
                : (yellow ? CRBlockPartials.SEMAPHORE_ARM_YELLOW_UPSIDE_DOWN : CRBlockPartials.SEMAPHORE_ARM_RED_UPSIDE_DOWN);
        } else {
            arm = flipped
                ? (yellow ? CRBlockPartials.SEMAPHORE_ARM_YELLOW_FLIPPED : CRBlockPartials.SEMAPHORE_ARM_RED_FLIPPED)
                : (yellow ? CRBlockPartials.SEMAPHORE_ARM_YELLOW : CRBlockPartials.SEMAPHORE_ARM_RED);
        }
        state.armBuf = CachedBuffers.partial(arm, blockState);

        boolean top = pos < 0.2;
        boolean bottom = pos > 0.8;
        float renderTime = AnimationTickHolder.getRenderTime(be.getLevel());
        // Blink the top lamp when invalid; steady when valid or arm is at bottom
        top = top && (renderTime % 40 < 3 || be.isValid);
        state.showLamp = top || bottom;

        if (state.showLamp) {
            // Lamp positions are in south-facing (local) block coordinates;
            // the Y rotation in submit() orients them to the actual facing direction
            state.lampTx = 8 / 16.0f;
            if (upside_down) {
                state.lampTy = bottom ? 9 / 16.0f : 4 / 16.0f;
            } else {
                state.lampTy = bottom ? 7 / 16.0f : 12 / 16.0f;
            }
            state.lampTz = bottom ? 15 / 16.0f : 14 / 16.0f;

            state.lampBuf = CachedBuffers.partial(AllPartialModels.SIGNAL_WHITE_CUBE, blockState);

            // Glow: white when proceed (bottom), yellow/red when stop (top)
            PartialModel glow = bottom ? AllPartialModels.SIGNAL_WHITE_GLOW
                : (yellow ? AllPartialModels.SIGNAL_YELLOW_GLOW : AllPartialModels.SIGNAL_RED_GLOW);
            state.glowBuf = CachedBuffers.partial(glow, blockState);

            PartialModel lamp = bottom ? CRBlockPartials.SEMAPHORE_LAMP_WHITE
                : (yellow ? CRBlockPartials.SEMAPHORE_LAMP_YELLOW : CRBlockPartials.SEMAPHORE_LAMP_RED);
            state.lampColorBuf = CachedBuffers.partial(lamp, blockState);

            state.translucentType = CreateRenderTypes.translucent();
            state.additiveType = CreateRenderTypes.additive();
        }
    }

    @Override
    public void submit(SemaphoreRenderState state, PoseStack matrices,
                       SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);

        if (state.armBuf != null) {
            queue.submitCustomGeometry(matrices, RenderTypes.cutoutMovingBlock(), (pose, consumer) ->
                state.armBuf
                    .rotateCenteredDegrees(state.yRot, Direction.Axis.Y)
                    .rotateCentered(state.armAngle, Direction.EAST)
                    .light(state.lightCoords)
                    .renderInto(pose, consumer));
        }

        if (state.showLamp) {
            if (state.lampBuf != null) {
                queue.submitCustomGeometry(matrices, state.translucentType, (pose, consumer) ->
                    state.lampBuf
                        .rotateCenteredDegrees(state.yRot, Direction.Axis.Y)
                        .translate(state.lampTx, state.lampTy, state.lampTz)
                        .light(0xF000F0)
                        .disableDiffuse()
                        .renderInto(pose, consumer));
            }

            OrderedSubmitNodeCollector additiveQueue = queue.order(1);

            if (state.glowBuf != null) {
                additiveQueue.submitCustomGeometry(matrices, state.additiveType, (pose, consumer) ->
                    state.glowBuf
                        .rotateCenteredDegrees(state.yRot, Direction.Axis.Y)
                        .translate(state.lampTx, state.lampTy, state.lampTz)
                        .light(0xF000F0)
                        .disableDiffuse()
                        .scale(1.5f, 2f, 2f)
                        .renderInto(pose, consumer));
            }

            if (state.lampColorBuf != null) {
                additiveQueue.submitCustomGeometry(matrices, state.additiveType, (pose, consumer) ->
                    state.lampColorBuf
                        .rotateCenteredDegrees(state.yRot, Direction.Axis.Y)
                        .translate(state.lampTx, state.lampTy, state.lampTz)
                        .light(0xF000F0)
                        .disableDiffuse()
                        .scale(1 + 1 / 16f)
                        .renderInto(pose, consumer));
            }
        }
    }

    public static class SemaphoreRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public BlockState blockState;
        public float yRot;
        public float armAngle;
        public @Nullable SuperByteBuffer armBuf;
        public boolean showLamp;
        public float lampTx, lampTy, lampTz;
        public @Nullable SuperByteBuffer lampBuf;
        public @Nullable SuperByteBuffer glowBuf;
        public @Nullable SuperByteBuffer lampColorBuf;
        public @Nullable RenderType translucentType;
        public @Nullable RenderType additiveType;
    }
}
