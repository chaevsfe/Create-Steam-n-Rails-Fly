/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.zurrtum.create.content.contraptions.actors.seat.SeatMovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SeatMovementBehaviour.class, remap = false)
public class MixinSeatMovementBehaviour {
    @Inject(
        method = "visitNewPosition",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;stopRiding()V", remap = true),
        cancellable = true
    )
    private void railways$keepConductorsSeated(
        MovementContext context,
        BlockPos pos,
        CallbackInfo ci,
        @Local(ordinal = 0) Entity toDismount
    ) {
        if (toDismount instanceof ConductorEntity)
            ci.cancel();
    }
}
