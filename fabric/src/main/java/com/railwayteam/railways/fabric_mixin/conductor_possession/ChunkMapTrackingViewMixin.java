/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.conductor_possession;

import com.railwayteam.railways.annotation.mixin.ConditionalMixin;
import com.railwayteam.railways.compat.Mods;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@ConditionalMixin(mods = Mods.VMP, applyIfPresent = false)
@Mixin(value = ChunkMap.class, priority = 1200)
public class ChunkMapTrackingViewMixin {
    @Redirect(
        method = "updateChunkTracking",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;chunkPosition()Lnet/minecraft/world/level/ChunkPos;"
        )
    )
    private ChunkPos railways$useConductorChunkForTrackingView(ServerPlayer player) {
        if (player.getCamera() instanceof ConductorEntity conductor)
            return conductor.chunkPosition();
        return player.chunkPosition();
    }
}
