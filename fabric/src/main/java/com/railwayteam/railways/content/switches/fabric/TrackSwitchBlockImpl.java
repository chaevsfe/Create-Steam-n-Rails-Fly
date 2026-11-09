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

package com.railwayteam.railways.content.switches.fabric;

import com.mojang.serialization.MapCodec;
import com.railwayteam.railways.content.switches.TrackSwitchBlock;

public class TrackSwitchBlockImpl extends TrackSwitchBlock {
    private static final MapCodec<TrackSwitchBlockImpl> CODEC = simpleCodec(properties -> new TrackSwitchBlockImpl(properties, false));

    protected TrackSwitchBlockImpl(Properties properties, boolean isAutomatic) {
        super(properties, isAutomatic);
    }

    @Override
    protected MapCodec<? extends TrackSwitchBlockImpl> codec() {
        return CODEC;
    }

    public static TrackSwitchBlock manual(Properties properties) {
        return new TrackSwitchBlockImpl(properties, false);
    }

    public static TrackSwitchBlock automatic(Properties properties) {
        return new TrackSwitchBlockImpl(properties, true);
    }
}
