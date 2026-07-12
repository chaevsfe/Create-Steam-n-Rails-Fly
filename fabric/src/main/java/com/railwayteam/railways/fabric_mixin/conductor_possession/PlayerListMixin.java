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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Routes positional broadcasts (notably sounds) to the possessed conductor's location. */
@Mixin(value = PlayerList.class, priority = 1200)
public class PlayerListMixin {
    @Redirect(
        method = "broadcast",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getX()D")
    )
    private double railways$useConductorXForBroadcast(ServerPlayer player) {
        return player.getCamera() instanceof ConductorEntity conductor ? conductor.getX() : player.getX();
    }

    @Redirect(
        method = "broadcast",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getY()D")
    )
    private double railways$useConductorYForBroadcast(ServerPlayer player) {
        return player.getCamera() instanceof ConductorEntity conductor ? conductor.getY() : player.getY();
    }

    @Redirect(
        method = "broadcast",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getZ()D")
    )
    private double railways$useConductorZForBroadcast(ServerPlayer player) {
        return player.getCamera() instanceof ConductorEntity conductor ? conductor.getZ() : player.getZ();
    }
}
