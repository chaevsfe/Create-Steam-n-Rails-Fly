/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.custom_tracks.phantom;

import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderState;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class PhantomTrackOverlays {
    public static final TrackBlockRenderState EMPTY = (ms, queue) -> {
    };

    private PhantomTrackOverlays() {
    }

    public static boolean isHidden(BlockGetter level, BlockPos pos, BlockState state, @Nullable BezierTrackPointLocation bezier) {
        return !PhantomSpriteManager.isVisible() && targetsPhantom(level, pos, state, bezier);
    }

    public static boolean mayTargetPhantom(Level level, TrackTargetingBehaviour<?> edgePoint) {
        return edgePoint.getTargetBezier() != null
            || level.getBlockState(edgePoint.getGlobalPosition()).getBlock() instanceof PhantomTrackBlock;
    }

    private static boolean targetsPhantom(BlockGetter level, BlockPos pos, BlockState state, @Nullable BezierTrackPointLocation bezier) {
        if (bezier == null)
            return state.getBlock() instanceof PhantomTrackBlock;
        return level.getBlockEntity(pos) instanceof TrackBlockEntity trackBE
            && trackBE.getConnections().get(bezier.curveTarget()) instanceof BezierConnection bc
            && bc.getMaterial() == CRTrackMaterials.PHANTOM;
    }
}
