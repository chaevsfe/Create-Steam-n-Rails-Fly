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
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Makes entity tracking distance use the possessed conductor instead of the stationary body. */
@Mixin(value = ChunkMap.TrackedEntity.class, priority = 1200)
public class TrackedEntityMixin {
    @Redirect(
        method = "updatePlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;"
        )
    )
    private Vec3 railways$useConductorPositionForEntityTracking(ServerPlayer player) {
        if (player.getCamera() instanceof ConductorEntity conductor)
            return conductor.position();
        return player.position();
    }
}
