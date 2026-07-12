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
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Forces all passengers to be captured and dismounted when their train is banished. */
@Mixin(value = Carriage.DimensionalCarriageEntity.class, remap = false)
public abstract class MixinDimensionalCarriageEntityShadow {
    @WrapOperation(
        method = "updatePassengerLoadout",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/trains/entity/CarriageContraptionEntity;isLocalCoordWithin(Lnet/minecraft/core/BlockPos;II)Z"
        )
    )
    private boolean railways$discardPassengersForShadow(
        CarriageContraptionEntity entity,
        BlockPos localPos,
        int min,
        int max,
        Operation<Boolean> original
    ) {
        if (((IShadowTrain) entity.getCarriage().train).railways$isShadow()) {
            return false;
        }
        return original.call(entity, localPos, min, max);
    }
}
