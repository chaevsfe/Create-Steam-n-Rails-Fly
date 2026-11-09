/*
 * Steam 'n' Rails
 * Copyright (c) 2023-2025 The Railways Team
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

package com.railwayteam.railways.content.custom_tracks.fabric;

import com.railwayteam.railways.content.custom_tracks.CustomTrackBlockStateGenerator;
import com.railwayteam.railways.content.custom_tracks.gen_template.OutputPrefixer;
import com.railwayteam.railways.content.custom_tracks.gen_template.TextureKey;
import com.railwayteam.railways.content.custom_tracks.gen_template.TrackGenTemplate;
import com.zurrtum.create.content.trains.track.TrackShape;

import java.util.Map;

public class CustomTrackBlockStateGeneratorImpl extends CustomTrackBlockStateGenerator {
    protected CustomTrackBlockStateGeneratorImpl(OutputPrefixer outputPrefixer, TrackGenTemplate template, Map<TrackShape, Map<String, TextureKey>> textureMap) {
        super(outputPrefixer, template, textureMap);
    }

    public static CustomTrackBlockStateGenerator create(OutputPrefixer outputPrefixer, TrackGenTemplate template, Map<TrackShape, Map<String, TextureKey>> textureMap) {
        return new CustomTrackBlockStateGeneratorImpl(outputPrefixer, template, textureMap);
    }
}
