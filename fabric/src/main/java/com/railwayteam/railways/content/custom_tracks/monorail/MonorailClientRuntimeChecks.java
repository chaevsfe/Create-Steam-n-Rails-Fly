/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.custom_tracks.monorail;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRShapes;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllTrackMaterialModels.TrackModelHolder;
import com.zurrtum.create.client.content.trains.track.RailwaysTrackVisualBridge;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.SegmentAngles;
import com.zurrtum.create.content.trains.track.BezierConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Development-only probes for monorail curve models, transforms, and selection shapes. */
public final class MonorailClientRuntimeChecks {
    private static boolean checked;

    private MonorailClientRuntimeChecks() {
    }

    public static void run() {
        if (checked || !Utils.isDevEnv())
            return;
        checked = true;

        checkModelHolder();
        checkCurveTransforms();
        checkSelectionShapes();
        Railways.LOGGER.info("Monorail curve model, segment transform, and selection runtime checks passed");
    }

    private static void checkModelHolder() {
        Object rawHolder = CRTrackMaterials.MONORAIL.getModelHolder();
        require(rawHolder instanceof TrackModelHolder, "monorail model holder was not registered");
        TrackModelHolder holder = (TrackModelHolder) rawHolder;
        require(holder.tie() == CRBlockPartials.MONORAIL_SEGMENT_MIDDLE,
            "curve middle model was not registered");
        require(holder.leftSegment() == CRBlockPartials.MONORAIL_SEGMENT_TOP,
            "curve top model was not registered");
        require(holder.rightSegment() == CRBlockPartials.MONORAIL_SEGMENT_BOTTOM,
            "curve bottom model was not registered");
    }

    private static void checkCurveTransforms() {
        Couple<BlockPos> positions = Couple.create(BlockPos.ZERO, new BlockPos(4, 0, 4));
        Couple<Vec3> starts = Couple.create(new Vec3(.5, .5, .5), new Vec3(4.5, .5, 4.5));
        Couple<Vec3> axes = Couple.create(new Vec3(1, 0, 0), new Vec3(0, 0, -1));
        Couple<Vec3> normals = Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0));
        BezierConnection connection = new BezierConnection(
            positions, starts, axes, normals, true, false, CRTrackMaterials.MONORAIL
        );

        SegmentAngles segments = RailwaysTrackVisualBridge.segmentAngles(connection);
        require(segments.length == connection.getSegmentCount(),
            "curve segment array length does not match Bezier segment count");
        require(segments.length > 0, "curve probe produced no render segments");
        for (int i = 0; i < segments.length; i++) {
            require(segments.lightPosition[i] != null, "curve light position " + i + " is null");
            require(segments.tieTransform[i] != null, "curve middle transform " + i + " is null");
            require(segments.railTransforms[i] != null,
                "curve cap transform pair " + i + " is null");
            require(segments.railTransforms[i].getFirst() != null,
                "curve top transform " + i + " is null");
            require(segments.railTransforms[i].getSecond() != null,
                "curve bottom transform " + i + " is null");
        }
    }

    private static void checkSelectionShapes() {
        require(CustomTrackBlockOutline.convert(
                AllShapes.TRACK_ORTHO.get(Direction.SOUTH), CRTrackMaterials.MONORAIL
            ) == CRShapes.MONORAIL_TRACK_ORTHO.get(Direction.SOUTH),
            "north-south monorail selection kept the standard rail shape");
        require(CustomTrackBlockOutline.convert(
                AllShapes.TRACK_ORTHO.get(Direction.EAST), CRTrackMaterials.MONORAIL
            ) == CRShapes.MONORAIL_TRACK_ORTHO.get(Direction.EAST),
            "east-west monorail selection kept the standard rail shape");
        require(CustomTrackBlockOutline.convert(AllShapes.TRACK_CROSS, CRTrackMaterials.MONORAIL)
                == CRShapes.MONORAIL_TRACK_CROSS,
            "crossing monorail selection kept the standard rail shape");
    }

    private static void require(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException("Monorail client runtime check failed: " + message);
    }
}
