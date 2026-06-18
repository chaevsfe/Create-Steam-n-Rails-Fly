package com.railwayteam.railways.util.fabric;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateUtilsImpl {
    public static SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        return state.getSoundType();
    }
}
