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

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.coupling.coupler.SecondaryTrackTargetingBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TrackEdgePoint.class, remap = false)
public abstract class MixinTrackEdgePointSecondary {
    @Shadow
    public abstract void write(ValueOutput view, DimensionPalette dimensions);

    @Inject(method = "invalidateAt(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;)V", at = @At("RETURN"))
    private void railways$invalidateSecondaryEdgePoint(LevelAccessor level, BlockPos tilePos, CallbackInfo ci) {
        SecondaryTrackTargetingBehaviour<?> behaviour = BlockEntityBehaviour.get(level, tilePos, SecondaryTrackTargetingBehaviour.TYPE);
        if (behaviour == null)
            return;
        try (ProblemReporter.ScopedCollector log = new ProblemReporter.ScopedCollector(() -> "TrackEdgePoint", Railways.LOGGER)) {
            TagValueOutput view = TagValueOutput.createWithContext(log, level.registryAccess());
            DimensionPalette dimensions = new DimensionPalette();
            write(view, dimensions);
            view.store("DimensionPalette", DimensionPalette.CODEC, dimensions);
            behaviour.invalidateEdgePoint(view.buildResult());
        }
    }
}
