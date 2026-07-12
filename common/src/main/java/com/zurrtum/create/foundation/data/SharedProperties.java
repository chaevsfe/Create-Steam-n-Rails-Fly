package com.zurrtum.create.foundation.data;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class SharedProperties {
    public static Block copperMetal() {
        return Blocks.COPPER_BLOCK.weathering().unaffected();
    }

    public static Block softMetal() {
        return Blocks.IRON_BLOCK;
    }

    public static Block stone() {
        return Blocks.STONE;
    }

    public static Block wooden() {
        return Blocks.OAK_PLANKS;
    }
}
