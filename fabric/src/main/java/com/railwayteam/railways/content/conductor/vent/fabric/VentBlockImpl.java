package com.railwayteam.railways.content.conductor.vent.fabric;

import com.railwayteam.railways.content.conductor.vent.VentBlock;

public class VentBlockImpl extends VentBlock {
    public VentBlockImpl(Properties properties) {
        super(properties);
    }

    public static VentBlock create(Properties properties) {
        return new VentBlockImpl(properties);
    }
}
