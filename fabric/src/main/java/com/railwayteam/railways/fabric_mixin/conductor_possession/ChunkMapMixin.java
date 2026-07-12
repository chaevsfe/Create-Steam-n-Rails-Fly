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

import com.railwayteam.railways.content.conductor.ConductorEntity;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Routes Minecraft 26.2's native player chunk-tracking view through a possessed conductor.
 *
 * <p>Unlike the legacy implementation, 26.2 no longer needs a second client chunk cache or
 * manual chunk-packet loops. {@link ChunkMap} owns a {@code ChunkTrackingView}; moving its player
 * origin to the camera makes vanilla update tickets, cache-center packets, chunk add/drop packets,
 * and block-update recipients as one coherent operation.</p>
 */
@Mixin(value = ChunkMap.class, priority = 1200)
public class ChunkMapMixin {
    @Redirect(
        method = {"move", "updatePlayerPos"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"
        )
    )
    private SectionPos railways$useConductorSectionForChunkTickets(EntityAccess entity) {
        if (entity instanceof ServerPlayer player && player.getCamera() instanceof ConductorEntity conductor)
            return SectionPos.of(conductor);
        return SectionPos.of(entity);
    }

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
