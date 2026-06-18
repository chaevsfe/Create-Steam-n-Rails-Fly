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

package com.railwayteam.railways.content.conductor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

public class ConductorRemoteLayer extends RenderLayer<ConductorRenderState, ConductorRenderModel> {

    private static final RenderType CUTOUT_BLOCKS = RenderType.create("railways_conductor_attachment",
            RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                    .withTexture("Sampler0", Identifier.withDefaultNamespace("textures/atlas/blocks.png"))
                    .useLightmap()
                    .createRenderSetup());

    public ConductorRemoteLayer(RenderLayerParent<ConductorRenderState, ConductorRenderModel> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitter, int packedLight,
                       ConductorRenderState state, float yRot, float xRot) {
        if (state.job == ConductorEntity.Job.REMOTE_CONTROL) {
            poseStack.pushPose();
            getParentModel().getHead().translateAndRotate(poseStack);
            renderPartial(submitter, poseStack,
                    CachedBuffers.partial(CRBlockPartials.CONDUCTOR_ANTENNA, Blocks.AIR.defaultBlockState())
                            .rotateXDegrees(180)
                            .translate(3 / 16.0, 3.5 / 16.0, 0 / 16.0)
                            .rotateZDegrees(-30)
                            .light(packedLight));
            poseStack.popPose();

        } else if (state.job == ConductorEntity.Job.SPY) {
            poseStack.pushPose();
            getParentModel().getHead().translateAndRotate(poseStack);
            renderPartial(submitter, poseStack,
                    CachedBuffers.partial(AllPartialModels.BLAZE_GOGGLES, Blocks.AIR.defaultBlockState())
                            .rotateZDegrees(180)
                            .translate(-8 / 16.0, 2 / 16.0, -8 / 16.0)
                            .light(packedLight));
            poseStack.popPose();
        }
    }

    @SuppressWarnings("unchecked")
    private static void renderPartial(SubmitNodeCollector submitter, PoseStack poseStack, SuperByteBuffer buf) {
        submitter.submitCustomGeometry(poseStack, CUTOUT_BLOCKS,
                (pose, consumer) -> buf.renderInto(pose, consumer));
    }
}
