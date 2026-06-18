package com.railwayteam.railways.content.coupling.coupler.fabric;

import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlock;

public class TrackCouplerBlockImpl extends TrackCouplerBlock {
    protected TrackCouplerBlockImpl(Properties properties) {
        super(properties);
    }

    public static TrackCouplerBlock create(Properties properties) {
        return new TrackCouplerBlockImpl(properties);
    }
}
