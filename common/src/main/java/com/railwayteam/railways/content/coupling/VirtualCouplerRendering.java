package com.railwayteam.railways.content.coupling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.content.trains.bogey.StandardBogeyBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;

public final class VirtualCouplerRendering {
    private VirtualCouplerRendering() {}

    public static void renderCoupler(Direction direction, double couplingDistance, boolean front, float partialTicks,
                                     PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     StandardBogeyBlockEntity te) {
    }
}
