/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.tank;

import com.railwayteam.railways.registry.fabric.CRBlockEntitiesImpl;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.item.ItemPlacementSoundContext;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FuelTankItem extends BlockItem {
    public FuelTankItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        if (result.consumesAction()) {
            tryMultiPlace(context);
        }
        return result;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
        BlockPos pos,
        Level level,
        @Nullable Player player,
        ItemStack stack,
        BlockState state
    ) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return false;
        }

        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data != null) {
            CompoundTag nbt = data.copyTagWithoutId();
            nbt.remove("Luminosity");
            nbt.remove("Size");
            nbt.remove("Height");
            nbt.remove("Controller");
            nbt.remove("LastKnownPos");
            capStoredFluid(server, nbt, "Fluid");
            capStoredFluid(server, nbt, "TankContent");
            stack.set(
                DataComponents.BLOCK_ENTITY_DATA,
                TypedEntityData.of(((IBE<?>) getBlock()).getBlockEntityType(), nbt)
            );
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    private static void capStoredFluid(MinecraftServer server, CompoundTag nbt, String key) {
        if (!nbt.contains(key)) {
            return;
        }
        FluidStack fluid = FluidStack.fromNbt(server.registryAccess(), nbt.getCompound(key));
        if (fluid.isEmpty()) {
            return;
        }
        fluid.setAmount(Math.min(FuelTankBlockEntity.getCapacityMultiplier(), fluid.getAmount()));
        nbt.put(key, fluid.toNbt(server.registryAccess()));
    }

    private void tryMultiPlace(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown() || SymmetryWandItem.presentInHotbar(player)) {
            return;
        }

        Direction face = context.getClickedFace();
        if (!face.getAxis().isVertical()) {
            return;
        }

        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos placedOnPos = pos.relative(face.getOpposite());
        if (!FuelTankBlock.isTank(level.getBlockState(placedOnPos))) {
            return;
        }

        FuelTankBlockEntity tankAt = ConnectivityHandler.partAt(
            CRBlockEntitiesImpl.FUEL_TANK.get(),
            level,
            placedOnPos
        );
        if (tankAt == null) {
            return;
        }
        FuelTankBlockEntity controller = (FuelTankBlockEntity) tankAt.getControllerBE();
        if (controller == null || controller.getWidth() == 1) {
            return;
        }

        int width = controller.getWidth();
        BlockPos startPos = face == Direction.DOWN
            ? controller.getBlockPos().below()
            : controller.getBlockPos().above(controller.getHeight());
        if (startPos.getY() != pos.getY()) {
            return;
        }

        int tanksToPlace = 0;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockState target = level.getBlockState(startPos.offset(x, 0, z));
                if (FuelTankBlock.isTank(target)) {
                    continue;
                }
                if (!target.canBeReplaced()) {
                    return;
                }
                tanksToPlace++;
            }
        }
        if (!player.isCreative() && stack.getCount() < tanksToPlace) {
            return;
        }

        ItemPlacementSoundContext quietContext = new ItemPlacementSoundContext(
            context,
            0.1f,
            1.5f,
            SILENCED_METAL.getPlaceSound()
        );
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                BlockPos targetPos = startPos.offset(x, 0, z);
                if (!FuelTankBlock.isTank(level.getBlockState(targetPos))) {
                    super.place(quietContext.offset(targetPos, face));
                }
            }
        }
    }

    public static final SoundType SILENCED_METAL = new SoundType(
        0.1F,
        1.5F,
        SoundEvents.METAL_BREAK,
        SoundEvents.METAL_STEP,
        SoundEvents.METAL_PLACE,
        SoundEvents.METAL_HIT,
        SoundEvents.METAL_FALL
    );
}
