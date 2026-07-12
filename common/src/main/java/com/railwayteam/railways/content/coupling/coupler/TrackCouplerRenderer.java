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

package com.railwayteam.railways.content.coupling.coupler;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.util.CustomTrackOverlayRendering;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TrackCouplerRenderer extends SmartBlockEntityRenderer<TrackCouplerBlockEntity, TrackCouplerRenderer.CouplerRenderState> {

    public TrackCouplerRenderer(Context context) {
        super(context);
    }

    @Override
    public CouplerRenderState createRenderState() {
        return new CouplerRenderState();
    }

    @Nullable
    public static PartialModel getCouplerOverlayModel(TrackCouplerBlockEntity te) {
        if (te.areEdgePointsOk()) {
            TrackCouplerBlockEntity.AllowedOperationMode mode = te.getAllowedOperationMode();
            if (mode.canCouple && mode.canDecouple) return CRBlockPartials.COUPLER_BOTH;
            if (mode.canCouple) return CRBlockPartials.COUPLER_COUPLE;
            if (mode.canDecouple) return CRBlockPartials.COUPLER_DECOUPLE;
        } else {
            return CRBlockPartials.COUPLER_NONE;
        }
        return null;
    }

    @Override
    public void extractRenderState(TrackCouplerBlockEntity te, CouplerRenderState state, float tickProgress, Vec3 cameraPos,
                                   @Nullable ModelFeatureRenderer.CrumblingOverlay crumbling) {
        super.extractRenderState(te, state, tickProgress, cameraPos, crumbling);
        state.clear();

        if (te.isRemoved())
            return;

        PartialModel model = getCouplerOverlayModel(te);
        if (model == null)
            return;

        addEdgePointState(te, state.first, te.edgePoint, model);
        addEdgePointState(te, state.second, te.secondEdgePoint, model);
    }

    private static void addEdgePointState(TrackCouplerBlockEntity te, EdgePointRenderState state,
                                          TrackTargetingBehaviour<TrackCoupler> target, PartialModel model) {
        if (target == null)
            return;

        BlockPos targetPosition = target.getGlobalPosition();
        Level level = te.getLevel();
        if (level == null)
            return;

        BlockState trackState = level.getBlockState(targetPosition);
        Block block = trackState.getBlock();

        if (!(block instanceof ITrackBlock))
            return;

        state.level = level;
        state.targetPosition = targetPosition;
        state.trackOffset = targetPosition.subtract(te.getBlockPos());
        state.trackState = trackState;
        state.targetDirection = target.getTargetDirection();
        state.targetBezier = target.getTargetBezier();
        state.overlayModel = model;
        state.offsetOverlayToSide = CustomTrackOverlayRendering.overlayWillOverlap(target);
    }

    @Override
    public void submit(CouplerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        submitEdgePoint(state.first, matrices, queue);
        submitEdgePoint(state.second, matrices, queue);
    }

    private static void submitEdgePoint(EdgePointRenderState state, PoseStack matrices, SubmitNodeCollector queue) {
        if (state.overlayModel == null || state.level == null || state.targetPosition == null || state.trackState == null)
            return;

        matrices.pushPose();
        matrices.translate(state.trackOffset.getX(), state.trackOffset.getY(), state.trackOffset.getZ());
        CustomTrackOverlayRendering.renderOverlay(state.level, state.targetPosition, state.targetDirection,
            state.targetBezier, matrices, queue, state.overlayModel, 1, state.offsetOverlayToSide);
        matrices.popPose();
    }

    public static class CouplerRenderState extends SmartBlockEntityRenderer.SmartRenderState {
        public final EdgePointRenderState first = new EdgePointRenderState();
        public final EdgePointRenderState second = new EdgePointRenderState();

        public void clear() {
            first.clear();
            second.clear();
        }
    }

    public static class EdgePointRenderState {
        public @Nullable Level level;
        public @Nullable BlockPos targetPosition;
        public @Nullable BlockPos trackOffset;
        public @Nullable BlockState trackState;
        public @Nullable Direction.AxisDirection targetDirection;
        public @Nullable BezierTrackPointLocation targetBezier;
        public @Nullable PartialModel overlayModel;
        public boolean offsetOverlayToSide;

        public void clear() {
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
