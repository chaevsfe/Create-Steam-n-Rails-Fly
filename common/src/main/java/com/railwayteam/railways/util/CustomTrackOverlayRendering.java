package com.railwayteam.railways.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

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
    }

    public static void renderOverlay(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                     BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     PartialModel model, float scale) {
    }

    public static void renderOverlay(LevelAccessor level, BlockPos pos, Direction.AxisDirection direction,
                                     BezierTrackPointLocation bezier, PoseStack ms, MultiBufferSource buffer, int light, int overlay,
                                     PartialModel model, float scale, boolean offsetToSide) {
    }

    public static boolean overlayWillOverlap(TrackTargetingBehaviour<? extends TrackEdgePoint> target) {
        return false;
    }
}
