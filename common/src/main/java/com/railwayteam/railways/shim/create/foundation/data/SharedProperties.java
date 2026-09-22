package com.railwayteam.railways.shim.create.foundation.data;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SharedProperties {
    public static Block copperMetal() {
        return Blocks.COPPER_BLOCK.weathering().unaffected();
    }

    public static Block softMetal() {
        return Blocks.GOLD_BLOCK;
    }

    public static Block stone() {
        return Blocks.ANDESITE;
    }

    public static Block wooden() {
        return Blocks.STRIPPED_SPRUCE_WOOD;
    }
}
