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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.entity.EntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ChunkMap.class, priority = 1200)
public class ChunkMapMixin {
    @WrapOperation(
        method = {
            "move(Lnet/minecraft/server/level/ServerPlayer;)V",
            "updatePlayerPos(Lnet/minecraft/server/level/ServerPlayer;)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/SectionPos;of(Lnet/minecraft/world/level/entity/EntityAccess;)Lnet/minecraft/core/SectionPos;"
        )
    )
    private SectionPos railways$useConductorSectionForChunkTickets(EntityAccess entity, Operation<SectionPos> original) {
        if (entity instanceof ServerPlayer player && player.getCamera() instanceof ConductorEntity conductor)
            return SectionPos.of(conductor);
        return original.call(entity);
    }
}
