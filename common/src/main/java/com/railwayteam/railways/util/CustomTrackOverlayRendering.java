package com.railwayteam.railways.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRBlockPartials.TrackCasingSpec;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.trains.track.TrackRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

public class CustomTrackOverlayRendering {
    public static final Map<EdgePointType<?>, PartialModel> CUSTOM_OVERLAYS = new HashMap<>();

    public static void register(EdgePointType<?> edgePointType, PartialModel model) {
        CUSTOM_OVERLAYS.put(edgePointType, model);
    }

    public static void renderOverlay(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                     BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     EdgePointType<?> type, float scale) {
        renderOverlayIfPresent(level, pos, direction, bezier, ms, buffer, light, overlay, type, scale);
    }

    public static boolean renderOverlayIfPresent(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                                 BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer,
                                                 int light, int overlay, EdgePointType<?> type, float scale) {
        if (!CUSTOM_OVERLAYS.containsKey(type))
            return false;
        return renderOverlay(level, pos, direction, bezier, ms, buffer, light, overlay, CUSTOM_OVERLAYS.get(type), scale, false);
    }

    public static void renderOverlay(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                     BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     PartialModel model, float scale) {
        renderOverlay(level, pos, direction, bezier, ms, buffer, light, overlay, model, scale, false);
    }

    public static boolean renderOverlay(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                        BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                        PartialModel model, float scale, boolean offsetToSide) {
        BlockState trackState = level.getBlockState(pos);
        return renderOverlay(level, pos, direction, bezier, ms, buffer, model, scale, offsetToSide, trackState);
    }

    private static boolean renderOverlay(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                         BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer,
                                         PartialModel model, float scale, boolean offsetToSide, BlockState trackState) {
        if (model == null)
            return false;

        boolean rendered = false;
        ms.pushPose();
        if (prepareTrackOverlay(level, pos, trackState, bezier, direction, ms)) {
            CachedBuffers.partial(model, trackState)
                .translate(.5, 0, .5)
                .scale(scale)
                .translate(offsetToSide ? .5 : -.5, 0, -.5)
                .light(LevelRenderer.getLightColor(level, pos))
                .renderInto(ms.last(), buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
            rendered = true;
        }
        ms.popPose();
        return rendered;
    }

    public static void renderOverlayInto(LevelAccessor level, BlockPos pos, BlockState trackState,
                                         Direction.AxisDirection direction, BezierTrackPointLocation bezier,
                                         PoseStack ms, PartialModel model, float scale, boolean offsetToSide,
                                         PoseStack.Pose pose, VertexConsumer consumer) {
        if (model == null)
            return;

        ms.pushPose();
        if (prepareTrackOverlay(level, pos, trackState, bezier, direction, ms)) {
            CachedBuffers.partial(model, trackState)
                .transform(ms.last())
                .translate(.5, 0, .5)
                .scale(scale)
                .translate(offsetToSide ? .5 : -.5, 0, -.5)
                .light(LevelRenderer.getLightColor(level, pos))
                .renderInto(pose, consumer);
        }
        ms.popPose();
    }

    private static boolean prepareTrackOverlay(LevelAccessor level, BlockPos pos, BlockState state,
                                               BezierTrackPointLocation bezierPoint, Direction.AxisDirection direction,
                                               PoseStack ms) {
        Block block = state.getBlock();
        if (!(block instanceof ITrackBlock track))
            return false;

        TransformStack<?> msr = TransformStack.of(ms);

        Vec3 axis = null;
        Vec3 diff = null;
        Vec3 normal = null;

        if (bezierPoint != null && level.getBlockEntity(pos) instanceof TrackBlockEntity trackBE) {
            BezierConnection bc = trackBE.getConnections().get(bezierPoint.curveTarget());
            if (bc == null)
                return false;

            if (bc.getMaterial() == CRTrackMaterials.PHANTOM && !PhantomSpriteManager.isVisible())
                return false;

            double length = Mth.floor(bc.getLength() * 2);
            int seg = bezierPoint.segment() + 1;
            double t = seg / length;
            double tpre = (seg - 1) / length;
            double tpost = (seg + 1) / length;

            Vec3 offset = bc.getPosition(t);
            normal = bc.getNormal(t);
            diff = bc.getPosition(tpost)
                .subtract(bc.getPosition(tpre))
                .normalize();

            Vec3 translate = offset.subtract(Vec3.atBottomCenterOf(pos));
            msr.translate(translate.x, translate.y, translate.z);
            msr.translate(0, -4 / 16f, 0);

            IHasTrackCasing casingBc = (IHasTrackCasing) bc;
            if (CRTrackMaterials.CRTrackType.MONORAIL.equals(CRTrackMaterials.getType(bc.getMaterial()))) {
                msr.translate(0, 14 / 16f, 0);
            } else if (casingBc.railways$getTrackCasing() != null) {
                if (bc.bePositions.getFirst().getY() == bc.bePositions.getSecond().getY()) {
                    msr.translate(0, 1 / 16f, 0);
                } else if (!casingBc.railways$isAlternate()) {
                    msr.translate(0, 4 / 16f, 0);
                }
            }
        }

        if (normal == null) {
            axis = state.getValue(TrackBlock.SHAPE)
                .getAxes()
                .getFirst();
            diff = axis.scale(direction.getStep())
                .normalize();
            normal = track.getUpNormal(level, pos, state);
        }

        if (block instanceof TrackBlock trackBlock && trackBlock.getMaterial() == CRTrackMaterials.PHANTOM && !PhantomSpriteManager.isVisible())
            return false;

        if (bezierPoint == null && block instanceof TrackBlock trackBlock
            && CRTrackMaterials.CRTrackType.MONORAIL.equals(CRTrackMaterials.getType(trackBlock.getMaterial()))) {
            msr.translate(0, 14 / 16f, 0);
        } else if (bezierPoint == null && level.getBlockEntity(pos) instanceof TrackBlockEntity trackBE
            && block instanceof TrackBlock trackBlock) {
            IHasTrackCasing casingTE = (IHasTrackCasing) trackBE;
            TrackShape shape = state.getValue(TrackBlock.SHAPE);
            if (casingTE.railways$getTrackCasing() != null) {
                TrackCasingSpec spec = CRBlockPartials.TRACK_CASINGS.get(shape);
                Identifier trackType = CRTrackMaterials.getType(trackBlock.getMaterial());
                if (spec != null)
                    msr.translate(
                        spec.getXShift(trackType),
                        (spec.getTopSurfacePixelHeight(trackType, casingTE.railways$isAlternate()) - 2) / 16f,
                        spec.getZShift(trackType)
                    );
            }
        }

        Vec3 angles = TrackRenderer.getModelAngles(normal, diff);
        msr.center()
            .rotateY((float) angles.y)
            .rotateX((float) angles.x)
            .uncenter();

        if (axis != null) {
            msr.translate(0, axis.y != 0 ? 7 / 16f : 0, axis.y != 0 ? direction.getStep() * 2.5f / 16f : 0);
        } else {
            msr.translate(0, 4 / 16f, 0);
            if (direction == Direction.AxisDirection.NEGATIVE)
                msr.rotateCentered(Mth.PI, Direction.UP);
        }

        if (bezierPoint == null && level.getBlockEntity(pos) instanceof TrackBlockEntity trackBE && trackBE.isTilted()) {
            double yOffset = 0;
            for (BezierConnection bc : trackBE.getConnections().values())
                yOffset += bc.starts.getFirst().y - pos.getY();
            msr.center()
                .rotateX((float) (-direction.getStep() * trackBE.tilt.smoothingAngle.get()))
                .uncenter()
                .translate(0, yOffset / 2, 0);
        }

        return true;
    }

    public static boolean overlayWillOverlap(TrackTargetingBehaviour<? extends TrackEdgePoint> target) {
        try {
            TrackGraphLocation graphLocation = target.determineGraphLocation();
            if (graphLocation == null)
                return false;

            TrackEdge edge = graphLocation.graph
                .getConnectionsFrom(graphLocation.graph.locateNode(graphLocation.edge.getFirst()))
                .get(graphLocation.graph.locateNode(graphLocation.edge.getSecond()));
            if (edge == null)
                return false;

            TrackEdgePoint targetPoint = target.getEdgePoint();
            for (TrackEdgePoint edgePoint : edge.getEdgeData().getPoints()) {
                try {
                    double targetLocation = targetPoint != null ? targetPoint.getLocationOn(edge) : graphLocation.position;
                    boolean samePoint = targetPoint != null && edgePoint.getId().equals(targetPoint.getId());
                    if (Math.abs(edgePoint.getLocationOn(edge) - targetLocation) < .75 && edgePoint != targetPoint && !samePoint)
                        return true;
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        return false;
    }
}
