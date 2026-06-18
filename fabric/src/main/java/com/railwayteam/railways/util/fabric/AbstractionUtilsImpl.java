package com.railwayteam.railways.util.fabric;

import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AbstractionUtilsImpl {
    public static BlockEntry<?> getFluidTankBlockEntry() { return null; }
    public static BlockEntry<?> getPortableFuelInterfaceBlockEntry() { return null; }
    public static boolean portableFuelInterfaceBlockHasState(BlockState state) { return false; }
    public static boolean isInstanceOfFuelTankBlockEntity(BlockEntity blockEntity) { return false; }
    public static boolean isInstanceOfFuelTankMountedStorageType(MountedFluidStorageType<?> type) { return false; }
}
