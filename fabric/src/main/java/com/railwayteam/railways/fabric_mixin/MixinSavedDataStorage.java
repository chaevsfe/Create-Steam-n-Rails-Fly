/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.mojang.serialization.Dynamic;
import com.railwayteam.railways.base.datafix.CRReferences;
import com.railwayteam.railways.base.datafixerapi.DataFixesInternals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

/**
 * Create 6 stores its railway network at {@code data/create/tracks.dat} with a null vanilla
 * DataFixType. Apply Railways' custom saved-data root after Create's null-type guard has returned.
 */
@Mixin(value = SavedDataStorage.class, priority = 1300)
public abstract class MixinSavedDataStorage {
    @Inject(
        method = "readTagFromDisk(Ljava/nio/file/Path;Lnet/minecraft/util/datafix/DataFixTypes;I)Lnet/minecraft/nbt/CompoundTag;",
        at = @At("RETURN"),
        cancellable = true
    )
    private void railways$updateCreateTracks(
        Path path,
        @Nullable DataFixTypes dataFixType,
        int targetVersion,
        CallbackInfoReturnable<CompoundTag> cir
    ) {
        if (!railways$isCreateTracks(path) || cir.getReturnValue() == null)
            return;

        CompoundTag fixed = (CompoundTag) DataFixesInternals.get().updateWithAllFixers(
            CRReferences.SAVED_DATA_CREATE_TRACKS,
            new Dynamic<>(NbtOps.INSTANCE, cir.getReturnValue())
        ).getValue();
        cir.setReturnValue(fixed);
    }

    @Unique
    private static boolean railways$isCreateTracks(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null)
            return false;
        if ("create_tracks.dat".equals(fileName.toString()))
            return true;
        if (!"tracks.dat".equals(fileName.toString()))
            return false;
        Path parent = path.getParent();
        return parent != null && parent.getFileName() != null && "create".equals(parent.getFileName().toString());
    }
}
