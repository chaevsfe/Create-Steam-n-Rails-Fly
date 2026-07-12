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

import com.railwayteam.railways.mixin_interfaces.IFuelInventory;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import com.zurrtum.create.infrastructure.fluids.SidedFluidInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class PortableFuelInterfaceBlockEntity extends PortableStorageInterfaceBlockEntity {
    public final FluidInventory capability;

    public PortableFuelInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        capability = new InterfaceFluidHandler();
    }

    @Override
    public void startTransferringTo(Contraption contraption, float distance) {
        MountedFluidStorageWrapper fuels = ((IFuelInventory) contraption.getStorage()).railways$getFluidFuels();
        ((InterfaceFluidHandler) capability).setInventory(fuels);
        super.startTransferringTo(contraption, distance);
    }

    @Override
    protected void stopTransferring() {
        ((InterfaceFluidHandler) capability).setEmpty();
        super.stopTransferring();
    }

    public class InterfaceFluidHandler implements SidedFluidInventory {
        private static final FluidTank EMPTY = new FluidTank(0);

        private int[] slots = SlotRangeCache.EMPTY;
        private FluidInventory wrapped = EMPTY;

        public void setInventory(@Nullable MountedFluidStorageWrapper inventory) {
            if (inventory == null) {
                setEmpty();
                return;
            }
            wrapped = inventory;
            slots = inventory.getAvailableSlots(null);
        }

        public void setEmpty() {
            wrapped = EMPTY;
            slots = SlotRangeCache.EMPTY;
        }

        @Override
        public int[] getAvailableSlots(@Nullable Direction side) {
            return slots;
        }

        @Override
        public boolean canExtract(int slot, FluidStack stack, Direction direction) {
            return wrapped != EMPTY && ((SidedFluidInventory) wrapped).canExtract(slot, stack, direction);
        }

        @Override
        public boolean canInsert(int slot, FluidStack stack, @Nullable Direction direction) {
            return wrapped != EMPTY && ((SidedFluidInventory) wrapped).canInsert(slot, stack, direction);
        }

        @Override
        public int size() {
            return slots.length;
        }

        @Override
        public int getMaxAmountPerStack() {
            return wrapped.getMaxAmountPerStack();
        }

        @Override
        public int getMaxAmount(FluidStack stack) {
            return wrapped.getMaxAmount(stack);
        }

        @Override
        public FluidStack getStack(int slot) {
            return wrapped.getStack(slot);
        }

        @Override
        public void setStack(int slot, FluidStack stack) {
            wrapped.setStack(slot, stack);
        }

        @Override
        public int insert(FluidStack stack, int maxAmount, @Nullable Direction side) {
            int inserted = wrapped.insert(stack, maxAmount, side);
            if (inserted != 0) {
                markDirty();
            }
            return inserted;
        }

        @Override
        public int extract(FluidStack stack, int maxAmount, @Nullable Direction side) {
            int extracted = wrapped.extract(stack, maxAmount, side);
            if (extracted != 0) {
                markDirty();
            }
            return extracted;
        }

        @Override
        public void markDirty() {
            if (wrapped != EMPTY) {
                onContentTransferred();
            }
        }

        @Override
        public java.util.Iterator<FluidStack> iterator(@Nullable Direction side) {
            return wrapped.iterator(side);
        }
    }
}
