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
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRShapes;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

/** Development-only probe for the monorail block factory and its physical shapes. */
public final class MonorailRuntimeChecks {
    private static boolean registered;
    private static boolean checked;

    private MonorailRuntimeChecks() {
    }

    public static void register() {
        if (registered || !Utils.isDevEnv())
            return;
        registered = true;
        ServerLifecycleEvents.SERVER_STARTED.register(MonorailRuntimeChecks::run);
    }

    private static void run(MinecraftServer server) {
        if (checked)
            return;
        checked = true;

        TrackBlock registeredBlock = CRBlocks.MONORAIL_TRACK.get();
        require(registeredBlock instanceof MonorailTrackBlock,
            "track_monorail was registered as " + registeredBlock.getClass().getName());
        require(CRTrackMaterials.MONORAIL.getBlock() == registeredBlock,
            "the monorail material points at a different block instance");

        BlockState state = registeredBlock.defaultBlockState()
            .setValue(TrackBlock.SHAPE, TrackShape.XO);
        require(state.getShape(server.overworld(), BlockPos.ZERO, CollisionContext.empty())
                == CRShapes.MONORAIL_TRACK_ORTHO.get(Direction.EAST),
            "straight monorail outline fell back to the standard track shape");
        require(state.getCollisionShape(server.overworld(), BlockPos.ZERO, CollisionContext.empty())
                == CRShapes.MONORAIL_COLLISION,
            "straight monorail collision fell back to the standard track shape");

        Railways.LOGGER.info("Monorail block factory, outline, and collision runtime checks passed");
    }

    private static void require(boolean condition, String message) {
        if (!condition)
            throw new IllegalStateException("Monorail runtime check failed: " + message);
    }
}
