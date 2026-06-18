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
