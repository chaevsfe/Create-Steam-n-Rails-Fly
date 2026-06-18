package com.railwayteam.railways.util.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OcclusionTestLevel implements BlockGetter {
    private final BlockGetter blockGetter;
    private final Map<BlockPos, BlockState> states = new HashMap<>();

    public OcclusionTestLevel(BlockGetter blockGetter) {
        this.blockGetter = blockGetter;
    }

    public void setBlock(BlockPos pos, BlockState state) {
        states.put(pos.immutable(), state);
    }

    public void clear() {
        states.clear();
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        return blockGetter.getBlockEntity(pos);
    }

    public BlockState getBlockState(BlockPos pos) {
        return states.getOrDefault(pos, blockGetter.getBlockState(pos));
    }

    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    public int getHeight() {
        return blockGetter.getHeight();
    }

    public int getMinY() {
        return blockGetter.getMinY();
    }
}
