package com.railwayteam.railways.content.smokestack.block.diesel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class DieselSmokeStackRenderer
    extends SmartBlockEntityRenderer<DieselSmokeStackBlockEntity, DieselSmokeStackRenderer.DieselRenderState> {
    public DieselSmokeStackRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public DieselRenderState createRenderState() {
        return new DieselRenderState();
    }

    @Override
    public void extractRenderState(DieselSmokeStackBlockEntity be, DieselRenderState state, float tickProgress,
                                   Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumbling);
        state.fan = null;
        if (be.isRemoved())
            return;

        BlockState blockState = be.getBlockState();
        Direction direction = blockState.getValue(DieselSmokeStackBlock.FACING);
        state.fan = CachedBuffers.partial(CRBlockPartials.DIESEL_STACK_FAN, blockState)
            .light(state.lightCoords)
            .translate(0.5, 0.5, 0.5)
            .rotateXDegrees(direction == Direction.DOWN ? 180 : direction.getAxis().isHorizontal() ? 90 : 0)
            .rotateZDegrees(direction.getAxis().isVertical() ? 0 : ((int) direction.toYRot()) % 360)
            .rotateYDegrees((float) be.getFanRotation(be.getRpm(tickProgress)))
            .translate(-0.5, -0.5, -0.5)
            .extractRenderState();
    }

    @Override
    public void submit(DieselRenderState state, PoseStack matrices, SubmitNodeCollector queue,
                       CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.fan != null)
            queue.submitCustomGeometry(matrices, RenderTypes.cutoutMovingBlock(), state.fan);
    }

    public static class DieselRenderState
        extends com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.SmartRenderState {
        public @Nullable SuperByteBufferRenderState fan;
    }
}
