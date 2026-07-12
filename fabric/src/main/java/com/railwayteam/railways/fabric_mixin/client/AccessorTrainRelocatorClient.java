/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.zurrtum.create.client.content.trains.entity.TrainRelocatorClient;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(value = TrainRelocatorClient.class, remap = false)
public interface AccessorTrainRelocatorClient {
    @Accessor("relocatingTrain")
    static void railways$setRelocatingTrain(UUID trainId) {
        throw new AssertionError("Mixin failed to apply");
    }

    @Accessor("relocatingOrigin")
    static void railways$setRelocatingOrigin(Vec3 origin) {
        throw new AssertionError("Mixin failed to apply");
    }

    @Accessor("relocatingEntityId")
    static void railways$setRelocatingEntityId(int entityId) {
        throw new AssertionError("Mixin failed to apply");
    }
}
