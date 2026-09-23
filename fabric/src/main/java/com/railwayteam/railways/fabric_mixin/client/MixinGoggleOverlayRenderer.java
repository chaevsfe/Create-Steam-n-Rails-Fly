/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.content.custom_bogeys.blocks.base.be.CRBogeyBlockEntity;
import com.railwayteam.railways.registry.fabric.client.CRGoggleTooltipBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.content.equipment.goggles.GoggleOverlayRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GoggleOverlayRenderer.class, remap = false)
public abstract class MixinGoggleOverlayRenderer {
    @WrapOperation(
        method = "renderOverlay",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/api/behaviour/BlockEntityBehaviour;get(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lcom/zurrtum/create/foundation/blockEntity/behaviour/BehaviourType;)Lcom/zurrtum/create/api/behaviour/BlockEntityBehaviour;"
        )
    )
    private static BlockEntityBehaviour<?> railways$bogeyGoggles(
        BlockGetter level,
        BlockPos pos,
        BehaviourType<?> type,
        Operation<BlockEntityBehaviour<?>> original
    ) {
        BlockEntityBehaviour<?> behaviour = original.call(level, pos, type);
        if (behaviour == null && type == TooltipBehaviour.TYPE && level.getBlockEntity(pos) instanceof CRBogeyBlockEntity bogey)
            return CRGoggleTooltipBehaviour.detached(bogey);
        return behaviour;
    }
}
