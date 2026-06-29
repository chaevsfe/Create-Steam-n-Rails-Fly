package com.railwayteam.railways.content.switches;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.util.CustomTrackOverlayRendering;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TrackSwitchRenderer
    extends SmartBlockEntityRenderer<TrackSwitchBlockEntity, TrackSwitchRenderer.SwitchRenderState> {

    public TrackSwitchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SwitchRenderState createRenderState() {
        return new SwitchRenderState();
    }

    @Override
    public void extractRenderState(TrackSwitchBlockEntity be, SwitchRenderState state, float tickProgress, Vec3 cameraPos,
                                   @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumbling);
        state.clear();

        if (be.isRemoved())
            return;

        BlockState blockState = state.blockState;
        state.yRot = AngleHelper.horizontalAngle(blockState.getValue(TrackSwitchBlock.FACING));
        state.automatic = be.isAutomatic();

        if (state.automatic) {
            state.flag = CachedBuffers.partial(CRBlockPartials.BRASS_SWITCH_FLAG, blockState);
            state.flagAngle = brassFlagAngle(be);
        } else {
            state.flag = CachedBuffers.partial(CRBlockPartials.ANDESITE_SWITCH_FLAG, blockState);
            state.handle = CachedBuffers.partial(CRBlockPartials.ANDESITE_SWITCH_HANDLE, blockState);
            state.flagAngle = andesiteFlagAngle(be);
        }

        Level level = be.getLevel();
        TrackTargetingBehaviour<TrackSwitch> target = be.edgePoint;
        if (level == null || target == null)
            return;

        BlockPos targetPosition = target.getGlobalPosition();
        BlockState trackState = level.getBlockState(targetPosition);
        Block block = trackState.getBlock();

        if (!(block instanceof ITrackBlock))
            return;

        state.level = level;
        state.targetPosition = targetPosition;
        state.trackOffset = targetPosition.subtract(be.getBlockPos());
        state.trackState = trackState;
        state.targetDirection = target.getTargetDirection();
        state.targetBezier = target.getTargetBezier();
        state.overlayModel = be.getOverlayModel();
        state.offsetOverlayToSide = CustomTrackOverlayRendering.overlayWillOverlap(target);
    }

    private static float brassFlagAngle(TrackSwitchBlockEntity be) {
        if (be.isReverseLeft() || (be.isNormal() && be.exitCount == 2 && be.hasExit(TrackSwitchBlock.SwitchState.REVERSE_RIGHT)))
            return -0.40f;
        if (be.isReverseRight() || (be.isNormal() && be.exitCount == 2 && be.hasExit(TrackSwitchBlock.SwitchState.REVERSE_LEFT)))
            return 0.40f;
        return 0.0f;
    }

    private static float andesiteFlagAngle(TrackSwitchBlockEntity be) {
        if (be.isReverseLeft())
            return 1.5708f;
        if (be.isReverseRight())
            return -1.5708f;
        return 0.0f;
    }

    @Override
    public void submit(SwitchRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);

        if (state.flag != null) {
            queue.submitCustomGeometry(matrices, RenderTypes.cutoutMovingBlock(), (pose, consumer) -> {
                SuperByteBuffer flag = state.flag
                    .rotateCenteredDegrees(state.yRot, Direction.Axis.Y)
                    .light(state.lightCoords);

                if (state.automatic) {
                    flag
                        .translate(0, -2.0 / 16, 0)
                        .rotateCentered(1.5708f, Direction.UP)
                        .translate(0.5, 8.5 / 16, 0.5)
                        .rotate(state.flagAngle, Direction.NORTH)
                        .translate(-0.5, -7.5 / 16, -0.5);
                } else {
                    flag.rotateCentered(state.flagAngle, Direction.UP);
                }

                flag.renderInto(pose, consumer);
            });
        }

        if (state.handle != null) {
            queue.submitCustomGeometry(matrices, RenderTypes.cutoutMovingBlock(), (pose, consumer) ->
                state.handle
                    .rotateCenteredDegrees(state.yRot, Direction.Axis.Y)
                    .rotateCentered(-1.5708f, Direction.UP)
                    .light(state.lightCoords)
                    .renderInto(pose, consumer));
        }

        if (state.overlayModel != null && state.level != null && state.targetPosition != null && state.trackState != null) {
            queue.submitCustomGeometry(matrices, RenderTypes.cutoutMovingBlock(), (pose, consumer) -> {
                PoseStack overlayMatrices = new PoseStack();
                overlayMatrices.translate(state.trackOffset.getX(), state.trackOffset.getY(), state.trackOffset.getZ());
                CustomTrackOverlayRendering.renderOverlayInto(state.level, state.targetPosition, state.trackState,
                    state.targetDirection, state.targetBezier, overlayMatrices, state.overlayModel, 1,
                    state.offsetOverlayToSide, pose, consumer);
            });
        }
    }

    public static class SwitchRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public float yRot;
        public boolean automatic;
        public float flagAngle;
        public @Nullable SuperByteBuffer flag;
        public @Nullable SuperByteBuffer handle;

        public @Nullable Level level;
        public @Nullable BlockPos targetPosition;
        public @Nullable BlockPos trackOffset;
        public @Nullable BlockState trackState;
        public @Nullable Direction.AxisDirection targetDirection;
        public @Nullable com.zurrtum.create.infrastructure.component.BezierTrackPointLocation targetBezier;
        public @Nullable PartialModel overlayModel;
        public boolean offsetOverlayToSide;

        public void clear() {
            flag = null;
            handle = null;
            level = null;
            targetPosition = null;
            trackOffset = null;
            trackState = null;
            targetDirection = null;
            targetBezier = null;
            overlayModel = null;
            offsetOverlayToSide = false;
        }
    }
}
