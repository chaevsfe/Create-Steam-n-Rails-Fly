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
import com.mojang.math.Axis;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.catnip.data.Iterate;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;

public class ConductorToolboxLayer extends RenderLayer<ConductorRenderState, ConductorRenderModel> {

    private static final RenderType CUTOUT_BLOCKS = RenderType.create("railways_conductor_toolbox",
            RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK)
                    .withTexture("Sampler0", Identifier.withDefaultNamespace("textures/atlas/blocks.png"))
                    .useLightmap()
                    .createRenderSetup());

    public ConductorToolboxLayer(RenderLayerParent<ConductorRenderState, ConductorRenderModel> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitter, int packedLight,
                       ConductorRenderState state, float yRot, float xRot) {
        if (!state.isCarryingToolbox || state.toolboxBlockState == null)
            return;

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.translate(-0.5, -1.2, -0.94);

        SuperByteBuffer body = CachedBuffers.partial(
                CRBlockPartials.TOOLBOX_BODIES.get(state.toolboxColor), state.toolboxBlockState)
                .light(packedLight);

        SuperByteBuffer lid = CachedBuffers.partial(
                AllPartialModels.TOOLBOX_LIDS.get(state.toolboxColor), state.toolboxBlockState)
                .translate(0, 6 / 16f, 12 / 16f)
                .rotateXDegrees(60 * state.toolboxLidAngle)
                .translate(0, -6 / 16f, -12 / 16f)
                .light(packedLight);

        renderBuf(submitter, poseStack, body);
        renderBuf(submitter, poseStack, lid);

        for (int offset : Iterate.zeroAndOne) {
            SuperByteBuffer drawer = CachedBuffers.partial(
                    AllPartialModels.TOOLBOX_DRAWER, state.toolboxBlockState)
                    .translate(0, offset / 8f, -state.toolboxDrawerOffset * .175f * (2 - offset))
                    .light(packedLight);
            renderBuf(submitter, poseStack, drawer);
        }

        poseStack.popPose();
    }

    @SuppressWarnings("unchecked")
    private static void renderBuf(SubmitNodeCollector submitter, PoseStack poseStack, SuperByteBuffer buf) {
        submitter.submitCustomGeometry(poseStack, CUTOUT_BLOCKS,
                (pose, consumer) -> buf.renderInto(pose, consumer));
    }
}
