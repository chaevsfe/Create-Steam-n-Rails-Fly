package com.railwayteam.railways.content.smokestack.block.diesel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.util.compat.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DieselSmokeStackRenderer extends SmartBlockEntityRenderer<DieselSmokeStackBlockEntity> {
    public DieselSmokeStackRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected void renderSafe(DieselSmokeStackBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
    }
}
