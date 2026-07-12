/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.psi;

import com.railwayteam.railways.registry.fabric.CRBlockEntitiesImpl;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PortableFuelInterfaceBlock extends WrenchableDirectionalBlock
    implements IBE<PortableFuelInterfaceBlockEntity>, FluidInventoryProvider<PortableFuelInterfaceBlockEntity> {

    public PortableFuelInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(
        BlockState state,
        Level level,
        BlockPos pos,
        Block neighbor,
        @Nullable Orientation orientation,
        boolean moved
    ) {
        withBlockEntityDo(level, pos, PortableFuelInterfaceBlockEntity::neighbourChanged);
    }

    @Override
    public void setPlacedBy(
        Level level,
        BlockPos pos,
        BlockState state,
        @Nullable LivingEntity placer,
        ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AdvancementBehaviour.setPlacedBy(level, pos, placer);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getNearestLookingDirection();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            direction = direction.getOpposite();
        }
        return defaultBlockState().setValue(FACING, direction.getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return AllShapes.PORTABLE_STORAGE_INTERFACE.get(state.getValue(FACING));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return getBlockEntityOptional(level, pos).map(be -> be.isConnected() ? 15 : 0).orElse(0);
    }

    @Override
    public FluidInventory getFluidInventory(
        LevelAccessor level,
        BlockPos pos,
        BlockState state,
        PortableFuelInterfaceBlockEntity blockEntity,
        @Nullable Direction context
    ) {
        return blockEntity.capability;
    }

    @Override
    public Class<PortableFuelInterfaceBlockEntity> getBlockEntityClass() {
        return PortableFuelInterfaceBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PortableFuelInterfaceBlockEntity> getBlockEntityType() {
        return CRBlockEntitiesImpl.PORTABLE_FUEL_INTERFACE.get();
    }
}
