package com.railwayteam.railways.content.conductor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ConductorCapLayer extends RenderLayer<ConductorRenderState, ConductorRenderModel> {
    private final ConductorCapModel<?> capModel;

    public ConductorCapLayer(RenderLayerParent<ConductorRenderState, ConductorRenderModel> renderer,
                             EntityModelSet modelSet) {
        super(renderer);
        capModel = new ConductorCapModel<>(modelSet.bakeLayer(ConductorCapModel.LAYER_LOCATION));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void submit(PoseStack poseStack, SubmitNodeCollector submitter, int packedLight,
                       ConductorRenderState state, float yRot, float xRot) {
        ItemStack stack = state.headStack;
        if (!(stack.getItem() instanceof ConductorCapItem cap))
            return;

        poseStack.pushPose();
        getParentModel().getHead().translateAndRotate(poseStack);
        submitter.submitModel((Model) capModel, state, poseStack, entityCutoutNoCull(cap.textureId),
            packedLight, LivingEntityRenderer.getOverlayCoords(state, 0), -1, null);
        poseStack.popPose();
    }

    public static RenderType entityCutoutNoCull(Identifier texture) {
        return RenderType.create("railways_conductor_cap", RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT_NO_CULL)
            .withTexture("Sampler0", texture)
            .useLightmap()
            .useOverlay()
            .createRenderSetup());
    }
}
