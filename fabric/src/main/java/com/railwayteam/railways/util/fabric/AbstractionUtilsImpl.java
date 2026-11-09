/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.util.fabric;

import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.railwayteam.railways.registry.fabric.CRBlocksImpl;
import com.railwayteam.railways.registry.fabric.CRMountedStorageTypesImpl;
import com.railwayteam.railways.shim.registrate.util.entry.BlockEntry;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class AbstractionUtilsImpl {
    private AbstractionUtilsImpl() {
    }

    public static BlockEntry<?> getFluidTankBlockEntry() {
        return CRBlocksImpl.FUEL_TANK;
    }

    public static BlockEntry<?> getPortableFuelInterfaceBlockEntry() {
        return CRBlocksImpl.PORTABLE_FUEL_INTERFACE;
    }

    public static boolean portableFuelInterfaceBlockHasState(BlockState state) {
        return CRBlocksImpl.PORTABLE_FUEL_INTERFACE.has(state);
    }

    public static boolean isInstanceOfFuelTankBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof FuelTankBlockEntity;
    }

    public static boolean isInstanceOfFuelTankMountedStorageType(MountedFluidStorageType<?> type) {
        return type == CRMountedStorageTypesImpl.FUEL_TANK;
    }
}
