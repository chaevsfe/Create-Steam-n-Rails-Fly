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
