/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.base.datafixerapi.DataFixesInternals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.IOWorker;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/** Ensures directly queued chunk NBT carries the independent Railways data version. */
@Mixin(IOWorker.class)
public abstract class MixinIOWorkerDataVersion {
    @Inject(
        method = "store(Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/nbt/CompoundTag;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("HEAD")
    )
    private void railways$addChunkDataVersion(
        ChunkPos chunkPos,
        @Nullable CompoundTag chunkData,
        CallbackInfoReturnable<CompletableFuture<Void>> cir
    ) {
        if (chunkData != null)
            DataFixesInternals.get().addModDataVersions(chunkData);
    }
}
