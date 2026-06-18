package com.railwayteam.railways.content.custom_tracks.casing.fabric;

import com.railwayteam.railways.content.custom_tracks.casing.CasingCollisionBlock;

public class CasingCollisionBlockImpl extends CasingCollisionBlock {
    public CasingCollisionBlockImpl(Properties properties) {
        super(properties);
    }

    public static CasingCollisionBlock create(Properties properties) {
        return new CasingCollisionBlockImpl(properties);
    }
}
