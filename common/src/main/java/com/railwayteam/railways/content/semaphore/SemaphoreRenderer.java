package com.railwayteam.railways.content.semaphore;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.util.compat.SafeBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class SemaphoreRenderer extends SafeBlockEntityRenderer<SemaphoreBlockEntity> {
    public SemaphoreRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected void renderSafe(SemaphoreBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
    }
}
