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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.mixin_interfaces.IShadowTrain;
import com.railwayteam.railways.mixin_interfaces.RailwaySavedDataDuck;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import com.zurrtum.create.content.trains.RailwaySavedData;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import net.minecraft.nbt.NbtOps;
import org.objectweb.asm.Opcodes;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Moves a newly banished train from the live manager into persistent shadow storage. */
@Mixin(value = GlobalRailwayManager.class, remap = false)
public abstract class MixinGlobalRailwayManagerShadow {
    @Shadow
    private @Nullable RailwaySavedData savedData;

    @WrapOperation(
        method = "tickTrains",
        at = @At(
            value = "FIELD",
            target = "Lcom/zurrtum/create/content/trains/entity/Train;invalid:Z",
            opcode = Opcodes.GETFIELD
        )
    )
    private boolean railways$removeShadowTrain(Train train, Operation<Boolean> original) {
        IShadowTrain shadowTrain = (IShadowTrain) train;
        if (!shadowTrain.railways$isShadow()) {
            return original.call(train);
        }
        if (savedData == null) {
            throw new IllegalStateException("Cannot persist a shadow train before RailwaySavedData is loaded");
        }

        // Carriage encoding captures the current contraption and passenger data before the
        // manager drops the train's live entities and broadcasts RemoveTrainPacket.
        DimensionPalette dimensions = new DimensionPalette();
        for (Carriage carriage : train.carriages) {
            Carriage.encode(carriage, NbtOps.INSTANCE, NbtOps.INSTANCE.empty(), dimensions).getOrThrow();
        }

        RailwaySavedDataDuck shadowData = (RailwaySavedDataDuck) savedData;
        shadowData.railway$getShadowTrains().put(train.id, train);
        shadowData.railways$getShadowKeys().put(shadowTrain.railways$getShadowKey(), train.id);
        savedData.setDirty();
        return true;
    }
}
