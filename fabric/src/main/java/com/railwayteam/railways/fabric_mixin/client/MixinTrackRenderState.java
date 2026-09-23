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

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils.CasingModel;
import com.railwayteam.railways.mixin_interfaces.ITrackCasingRenderState;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.TrackRenderState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(value = TrackRenderState.class, remap = false)
public class MixinTrackRenderState implements ITrackCasingRenderState {
    @Unique
    private @Nullable List<CasingModel> railways$casings;

    @Override
    public void railways$setCasings(@Nullable List<CasingModel> casings) {
        railways$casings = casings;
    }

    @Override
    public @Nullable List<CasingModel> railways$getCasings() {
        return railways$casings;
    }
}
