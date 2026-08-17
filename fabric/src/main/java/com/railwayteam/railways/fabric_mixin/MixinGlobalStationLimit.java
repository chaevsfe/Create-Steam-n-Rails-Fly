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

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.ILimitedGlobalStation;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.station.GlobalStation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GlobalStation.class, remap = false)
public abstract class MixinGlobalStationLimit implements ILimitedGlobalStation {
    @Unique
    private static final String RAILWAYS$LIMIT_KEY = "LimitEnabled";

    @Shadow
    @Nullable
    public abstract Train getNearestTrain();

    @Unique
    private boolean railways$limitEnabled;

    @Override
    public boolean isStationEnabled() {
        return !railways$limitEnabled || getNearestTrain() == null;
    }

    @Override
    public Train getDisablingTrain() {
        return railways$limitEnabled ? getNearestTrain() : null;
    }

    @Override
    public Train orDisablingTrain(Train before, Train except) {
        if (before == null || before == except)
            before = getDisablingTrain();
        return before;
    }

    @Override
    public void setLimitEnabled(boolean limitEnabled) {
        this.railways$limitEnabled = limitEnabled;
    }

    @Override
    public boolean isLimitEnabled() {
        return railways$limitEnabled;
    }

    @Inject(method = "read(Lnet/minecraft/world/level/storage/ValueInput;ZLcom/zurrtum/create/content/trains/graph/DimensionPalette;)V", at = @At("TAIL"))
    private void railways$readLimit(ValueInput input, boolean migration, DimensionPalette dimensions, CallbackInfo ci) {
        railways$limitEnabled = input.getBooleanOr(RAILWAYS$LIMIT_KEY, false);
    }

    @Inject(method = "write(Lnet/minecraft/world/level/storage/ValueOutput;Lcom/zurrtum/create/content/trains/graph/DimensionPalette;)V", at = @At("TAIL"))
    private void railways$writeLimit(ValueOutput output, DimensionPalette dimensions, CallbackInfo ci) {
        output.putBoolean(RAILWAYS$LIMIT_KEY, railways$limitEnabled);
    }

    @Inject(method = "decode", at = @At("TAIL"))
    private <T> void railways$decodeLimit(DynamicOps<T> ops, T input, boolean migration, DimensionPalette dimensions, CallbackInfo ci) {
        railways$limitEnabled = ops.getMap(input).result()
            .map(map -> railways$readLimitFrom(ops, map))
            .orElse(false);
    }

    @Unique
    private <T> boolean railways$readLimitFrom(DynamicOps<T> ops, MapLike<T> map) {
        T value = map.get(RAILWAYS$LIMIT_KEY);
        if (value == null)
            return false;
        return ops.getBooleanValue(value).result().orElse(false);
    }

    @WrapOperation(
        method = "encode",
        at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/DynamicOps;mapBuilder()Lcom/mojang/serialization/RecordBuilder;")
    )
    private <T> RecordBuilder<T> railways$encodeLimit(DynamicOps<T> ops, Operation<RecordBuilder<T>> original) {
        return original.call(ops).add(RAILWAYS$LIMIT_KEY, ops.createBoolean(railways$limitEnabled));
    }

    @Inject(method = "read(Lnet/minecraft/network/FriendlyByteBuf;Lcom/zurrtum/create/content/trains/graph/DimensionPalette;)V", at = @At("TAIL"))
    private void railways$readNetLimit(FriendlyByteBuf buffer, DimensionPalette dimensions, CallbackInfo ci) {
        railways$limitEnabled = buffer.readBoolean();
    }

    @Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;Lcom/zurrtum/create/content/trains/graph/DimensionPalette;)V", at = @At("TAIL"))
    private void railways$writeNetLimit(FriendlyByteBuf buffer, DimensionPalette dimensions, CallbackInfo ci) {
        buffer.writeBoolean(railways$limitEnabled);
    }
}
