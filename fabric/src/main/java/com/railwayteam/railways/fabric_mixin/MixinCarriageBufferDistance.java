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

import com.railwayteam.railways.mixin_interfaces.ICarriageBufferDistanceTracker;
import com.zurrtum.create.content.trains.entity.Carriage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Carriage.class, remap = false)
public abstract class MixinCarriageBufferDistance implements ICarriageBufferDistanceTracker {
    @Unique
    private @Nullable Integer railways$leadingBufferDistance = null;

    @Unique
    private @Nullable Integer railways$trailingBufferDistance = null;

    @Override
    public @Nullable Integer railways$getLeadingDistance() {
        return railways$leadingBufferDistance;
    }

    @Override
    public @Nullable Integer railways$getTrailingDistance() {
        return railways$trailingBufferDistance;
    }

    @Override
    public void railways$setLeadingDistance(int distance) {
        railways$leadingBufferDistance = distance;
    }

    @Override
    public void railways$setTrailingDistance(int distance) {
        railways$trailingBufferDistance = distance;
    }
}
