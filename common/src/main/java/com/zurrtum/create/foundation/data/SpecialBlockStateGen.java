package com.zurrtum.create.foundation.data;

import net.minecraft.world.level.block.state.BlockState;

public abstract class SpecialBlockStateGen {
    protected int getXRotation(BlockState state) {
        return 0;
    }

    protected int getYRotation(BlockState state) {
        return 0;
    }
}
