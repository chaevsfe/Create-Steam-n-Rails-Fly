/*
 * Steam 'n' Rails
 * Copyright (c) 2025-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.tank;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.railwayteam.railways.registry.fabric.CRMountedStorageTypesImpl;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.contraption.storage.SyncedMountedStorage;
import com.zurrtum.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FuelTankMountedStorage
    extends WrapperMountedFluidStorage<FuelTankMountedStorage.Handler>
    implements SyncedMountedStorage {

    public static final MapCodec<FuelTankMountedStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(FuelTankMountedStorage::getCapacity),
        FluidStack.OPTIONAL_CODEC.fieldOf("fluid").forGetter(FuelTankMountedStorage::getFluid)
    ).apply(instance, FuelTankMountedStorage::new));

    private boolean dirty;

    protected FuelTankMountedStorage(int capacity, FluidStack stack) {
        super(CRMountedStorageTypesImpl.FUEL_TANK);
        wrapped = new Handler(capacity, stack);
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof FuelTankBlockEntity tank && tank.isController()) {
            tank.getTankInventory().setFluid(wrapped.getFluid());
        }
    }

    public FluidStack getFluid() {
        return wrapped.getFluid();
    }

    public int getCapacity() {
        return wrapped.getMaxAmountPerStack();
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markClean() {
        dirty = false;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public void afterSync(Contraption contraption, BlockPos localPos) {
        BlockEntity be = AllClientHandle.INSTANCE.getBlockEntityClientSide(contraption, localPos);
        if (!(be instanceof FuelTankBlockEntity tank)) {
            return;
        }

        FluidTank inventory = tank.getTankInventory();
        inventory.setFluid(getFluid());
        float fillLevel = inventory.getFluid().getAmount() / (float) inventory.getMaxAmountPerStack();
        if (tank.getFluidLevel() == null) {
            tank.setFluidLevel(LerpedFloat.linear().startWithValue(fillLevel));
        }
        tank.getFluidLevel().chase(fillLevel, 0.5, LerpedFloat.Chaser.EXP);
    }

    public static FuelTankMountedStorage fromTank(FuelTankBlockEntity tank) {
        FluidTank inventory = tank.getTankInventory();
        return new FuelTankMountedStorage(inventory.getMaxAmountPerStack(), inventory.getFluid().copy());
    }

    public final class Handler extends FluidTank {
        private Handler(int capacity, FluidStack stack) {
            super(capacity);
            setFluid(stack);
        }

        @Override
        public boolean isValid(int slot, FluidStack stack) {
            return tankFuel(stack);
        }

        @Override
        public void markDirty() {
            dirty = true;
        }
    }

    private static boolean tankFuel(FluidStack stack) {
        return com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler.isFuelForTanks(stack.getFluid());
    }
}
