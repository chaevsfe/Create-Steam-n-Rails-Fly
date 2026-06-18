package com.railwayteam.railways.util.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Temporary bridge for the pre-1.21 immediate-mode block entity renderer API.
 * Rendering is intentionally no-op until these renderers are ported to submit/render states.
 */
public abstract class SmartBlockEntityRenderer<T extends SmartBlockEntity>
    extends com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer<T, com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.SmartRenderState> {
    protected SmartBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected void renderSafe(T blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
    }
}
