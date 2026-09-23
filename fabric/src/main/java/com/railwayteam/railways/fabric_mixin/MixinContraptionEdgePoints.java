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
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Contraption.class, remap = false)
public abstract class MixinContraptionEdgePoints {
    @WrapOperation(
        method = "removeBlocksFromWorld",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V")
    )
    private void railways$releaseEdgePoints(Level level, BlockPos pos, Operation<Void> original) {
        MinecraftServer server = level.getServer();
        if (!level.isClientSide() && server != null && level.getBlockEntity(pos) instanceof SmartBlockEntity smartBlockEntity) {
            for (BlockEntityBehaviour<?> behaviour : smartBlockEntity.getAllBehaviours()) {
                if (!(behaviour instanceof TrackTargetingBehaviour<?> target))
                    continue;
                TrackEdgePoint edgePoint = target.getEdgePoint();
                if (edgePoint != null)
                    edgePoint.blockEntityRemoved(server, pos, target.getTargetDirection() == Direction.AxisDirection.POSITIVE);
            }
        }
        original.call(level, pos);
    }
}
