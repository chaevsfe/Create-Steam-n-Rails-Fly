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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.config.CRConfigs;
import com.zurrtum.create.AllHandle;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AllHandle.class, remap = false)
public class MixinAllHandleRelocationRange {
    @WrapOperation(
        method = "onTrainRelocation",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;closerThan(Lnet/minecraft/core/Position;D)Z")
    )
    private static boolean railways$unrestrictRelocationRange(
        Vec3 instance,
        Position origin,
        double distance,
        Operation<Boolean> original,
        @Local(argsOnly = true) ServerGamePacketListenerImpl listener
    ) {
        ServerPlayer sender = listener.player;
        if (sender != null && sender.isCreative() && CRConfigs.server().unlimitedCreativeRelocation.get())
            return true;

        return original.call(instance, origin, distance);
    }
}
