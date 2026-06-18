package com.railwayteam.railways.content.conductor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

// Placeholder — secondary head items (goggles, antenna) are rendered by ConductorRemoteLayer.
public class ConductorSecondaryHeadRenderLayer extends RenderLayer<ConductorRenderState, ConductorRenderModel> {
    public ConductorSecondaryHeadRenderLayer(RenderLayerParent<ConductorRenderState, ConductorRenderModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitter, int packedLight,
                       ConductorRenderState state, float yRot, float xRot) {
    }
}
