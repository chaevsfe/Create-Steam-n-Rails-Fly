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
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.IShadowTrain;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

/** Adds the persistent upstream-compatible ShadowKey field to Create Fly trains. */
@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainShadow implements IShadowTrain {
    @Unique
    private @Nullable Identifier railways$shadowKey;

    @Override
    public void railways$setShadow(@NotNull Identifier shadowKey) {
        railways$shadowKey = shadowKey;
    }

    @Override
    public void railways$clearShadow() {
        railways$shadowKey = null;
    }

    @Override
    public @Nullable Identifier railways$getShadowKey() {
        return railways$shadowKey;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void railways$writeShadowKey(
        ValueOutput view,
        DimensionPalette dimensions,
        CallbackInfo ci
    ) {
        if (railways$shadowKey != null) {
            view.store("ShadowKey", Identifier.CODEC, railways$shadowKey);
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void railways$readShadowKey(
        ValueInput view,
        Map<UUID, TrackGraph> trackNetworks,
        DimensionPalette dimensions,
        CallbackInfoReturnable<Train> cir
    ) {
        IShadowTrain shadowTrain = (IShadowTrain) cir.getReturnValue();
        view.read("ShadowKey", Identifier.CODEC).ifPresentOrElse(
            shadowTrain::railways$setShadow,
            shadowTrain::railways$clearShadow
        );
    }

    @WrapOperation(
        method = "encode",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/DynamicOps;mapBuilder()Lcom/mojang/serialization/RecordBuilder;",
            ordinal = 0
        )
    )
    private static <T> RecordBuilder<T> railways$encodeShadowKey(
        DynamicOps<T> ops,
        Operation<RecordBuilder<T>> original,
        @Local(argsOnly = true) Train input
    ) {
        RecordBuilder<T> builder = original.call(ops);
        Identifier shadowKey = ((IShadowTrain) input).railways$getShadowKey();
        if (shadowKey != null) {
            builder.add("ShadowKey", shadowKey, Identifier.CODEC);
        }
        return builder;
    }

    @Inject(method = "decode", at = @At("RETURN"))
    private static <T> void railways$decodeShadowKey(
        DynamicOps<T> ops,
        T input,
        Map<UUID, TrackGraph> trackNetworks,
        DimensionPalette dimensions,
        CallbackInfoReturnable<Train> cir
    ) {
        IShadowTrain shadowTrain = (IShadowTrain) cir.getReturnValue();
        MapLike<T> map = ops.getMap(input).getOrThrow();
        T encodedKey = map.get("ShadowKey");
        if (encodedKey == null) {
            shadowTrain.railways$clearShadow();
            return;
        }
        Identifier.CODEC.parse(ops, encodedKey).result().ifPresentOrElse(
            shadowTrain::railways$setShadow,
            shadowTrain::railways$clearShadow
        );
    }
}
