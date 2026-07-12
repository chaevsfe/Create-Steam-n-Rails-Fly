/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.tank;

import com.google.common.collect.ImmutableMap;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler;
import com.railwayteam.railways.multiloader.fluid.MultiloaderFluidStack;
import com.railwayteam.railways.registry.fabric.CRBlockEntitiesImpl;
import com.railwayteam.railways.registry.fabric.CRBlocksImpl;
import com.railwayteam.railways.registry.fabric.CRMountedStorageTypesImpl;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.material.Fluids;

/** Development-only end-to-end probes for the restored fuel subsystem. */
public final class FuelRuntimeChecks {
    private static boolean registered;
    private static boolean checked;

    private FuelRuntimeChecks() {
    }

    public static void register() {
        if (registered || !Utils.isDevEnv()) {
            return;
        }
        registered = true;
        ServerLifecycleEvents.SERVER_STARTED.register(FuelRuntimeChecks::run);
    }

    private static void run(MinecraftServer server) {
        if (checked) {
            return;
        }
        checked = true;

        checkRegistries();
        checkFuelInventoryAndDraining(server);
        checkMultiloaderStackRoundTrip();
        Railways.LOGGER.info(
            "Fuel block, block-entity, mounted-storage, movement, liquid-drain, and fluid-stack runtime checks passed"
        );
    }

    private static void checkRegistries() {
        require(
            BuiltInRegistries.BLOCK.getValue(Railways.asResource("fuel_tank")) == CRBlocksImpl.FUEL_TANK.get(),
            "fuel tank block is not registered"
        );
        require(
            BuiltInRegistries.ITEM.getValue(Railways.asResource("fuel_tank")) == CRBlocksImpl.FUEL_TANK.asItem(),
            "fuel tank item is not registered"
        );
        require(
            BuiltInRegistries.BLOCK.getValue(Railways.asResource("portable_fuel_interface"))
                == CRBlocksImpl.PORTABLE_FUEL_INTERFACE.get(),
            "portable fuel interface block is not registered"
        );
        require(
            BuiltInRegistries.ITEM.getValue(Railways.asResource("portable_fuel_interface"))
                == CRBlocksImpl.PORTABLE_FUEL_INTERFACE.asItem(),
            "portable fuel interface item is not registered"
        );
        require(
            BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(Railways.asResource("fuel_tank"))
                == CRBlockEntitiesImpl.FUEL_TANK.get(),
            "fuel tank block entity type is not registered"
        );
        require(
            BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(Railways.asResource("portable_fuel_interface"))
                == CRBlockEntitiesImpl.PORTABLE_FUEL_INTERFACE.get(),
            "portable fuel interface block entity type is not registered"
        );
        require(
            CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE.getValue(Railways.asResource("fuel_tank"))
                == CRMountedStorageTypesImpl.FUEL_TANK,
            "fuel tank mounted storage type is not registered"
        );
        require(
            MountedFluidStorageType.REGISTRY.get(CRBlocksImpl.FUEL_TANK.get())
                == CRMountedStorageTypesImpl.FUEL_TANK,
            "fuel tank block is not mapped to its mounted storage type"
        );
        require(
            MovementBehaviour.REGISTRY.get(CRBlocksImpl.FUEL_TANK.get()) == CRBlocksImpl.FUEL_TANK_MOVEMENT,
            "fuel tank movement behaviour is not registered"
        );
        require(
            MovementBehaviour.REGISTRY.get(CRBlocksImpl.PORTABLE_FUEL_INTERFACE.get())
                == CRBlocksImpl.PORTABLE_FUEL_INTERFACE_MOVEMENT,
            "portable fuel interface movement behaviour is not registered"
        );
    }

    private static void checkFuelInventoryAndDraining(MinecraftServer server) {
        FluidStack lava = new FluidStack(Fluids.LAVA, 16_200);
        FuelTankMountedStorage storage = new FuelTankMountedStorage(16_200, lava);
        Tag encodedStorage = MountedFluidStorage.CODEC.encodeStart(
            server.registryAccess().createSerializationContext(NbtOps.INSTANCE),
            storage
        ).getOrThrow();
        MountedFluidStorage decodedStorage = MountedFluidStorage.CODEC.parse(
            server.registryAccess().createSerializationContext(NbtOps.INSTANCE),
            encodedStorage
        ).getOrThrow();
        require(
            decodedStorage instanceof FuelTankMountedStorage decoded
                && decoded.getCapacity() == 16_200
                && decoded.getFluid().isOf(Fluids.LAVA)
                && decoded.getFluid().getAmount() == 16_200,
            "mounted fuel tank MapCodec round-trip failed"
        );

        MountedFluidStorageWrapper wrapper = new MountedFluidStorageWrapper(
            ImmutableMap.of(BlockPos.ZERO, storage)
        );

        int fuelTicks = LiquidFuelTrainHandler.handleFuelDraining(wrapper, server.fuelValues());
        require(fuelTicks > 0, "one tenth of a lava bucket produced no train fuel ticks");
        require(wrapper.getStack(0).getAmount() == 8_100, "liquid fuel drain did not consume exactly 8,100 units");
        require(storage.isDirty(), "liquid fuel drain did not mark mounted storage dirty");

        FuelTankBlockEntity blockEntity = new FuelTankBlockEntity(
            CRBlockEntitiesImpl.FUEL_TANK.get(),
            BlockPos.ZERO,
            CRBlocksImpl.FUEL_TANK.getDefaultState()
        );
        require(
            blockEntity.getTankInventory().isValid(0, new FluidStack(Fluids.LAVA, 8_100)),
            "realistic fuel tank rejected vanilla lava"
        );
    }

    private static void checkMultiloaderStackRoundTrip() {
        CompoundTag metadata = new CompoundTag();
        metadata.putString("probe", "fuel");
        MultiloaderFluidStack original = MultiloaderFluidStack.create(Fluids.LAVA, 8_100, metadata);
        MultiloaderFluidStack codecRoundTrip = MultiloaderFluidStack.CODEC
            .parse(NbtOps.INSTANCE, MultiloaderFluidStack.CODEC.encodeStart(NbtOps.INSTANCE, original).getOrThrow())
            .getOrThrow();
        require(original.isFluidStackIdentical(codecRoundTrip), "MultiloaderFluidStack codec round-trip failed");

        MultiloaderFluidStack nbtRoundTrip = MultiloaderFluidStack.loadFluidStackFromNBT(
            original.writeToNBT(new CompoundTag())
        );
        require(original.isFluidStackIdentical(nbtRoundTrip), "MultiloaderFluidStack NBT round-trip failed");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Fuel runtime check failed: " + message);
        }
    }
}
