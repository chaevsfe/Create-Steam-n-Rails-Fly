package com.railwayteam.railways.content.switches;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.util.compat.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class TrackSwitchRenderer extends SmartBlockEntityRenderer<TrackSwitchBlockEntity> {
    public TrackSwitchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected void renderSafe(TrackSwitchBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
    }
}
