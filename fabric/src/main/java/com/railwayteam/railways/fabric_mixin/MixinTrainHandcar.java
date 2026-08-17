package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(value = Train.class, remap = false)
public class MixinTrainHandcar implements IHandcarTrain {
    @Unique
    private boolean railways$isHandcar = false;

    @Override
    public boolean railways$isHandcar() {
        return railways$isHandcar;
    }

    @Override
    public void railways$setHandcar(boolean handcar) {
        railways$isHandcar = handcar;
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void railways$writeHandcar(ValueOutput view, DimensionPalette dimensions, CallbackInfo ci) {
        view.putBoolean("IsHandcar", railways$isHandcar);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void railways$readHandcar(ValueInput view, Map<UUID, TrackGraph> trackNetworks,
                                             DimensionPalette dimensions,
                                             CallbackInfoReturnable<Train> cir) {
        ((IHandcarTrain) cir.getReturnValue()).railways$setHandcar(view.getBooleanOr("IsHandcar", false));
    }

    @WrapOperation(
        method = "encode",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/serialization/DynamicOps;mapBuilder()Lcom/mojang/serialization/RecordBuilder;",
            ordinal = 0
        )
    )
    private static <T> RecordBuilder<T> railways$encodeHandcar(
        DynamicOps<T> ops,
        Operation<RecordBuilder<T>> original,
        @Local(argsOnly = true) Train input
    ) {
        RecordBuilder<T> builder = original.call(ops);
        if (((IHandcarTrain) input).railways$isHandcar()) {
            builder.add("IsHandcar", true, Codec.BOOL);
        }
        return builder;
    }

    @Inject(method = "decode", at = @At("RETURN"))
    private static <T> void railways$decodeHandcar(
        DynamicOps<T> ops,
        T input,
        Map<UUID, TrackGraph> trackNetworks,
        DimensionPalette dimensions,
        CallbackInfoReturnable<Train> cir
    ) {
        IHandcarTrain train = (IHandcarTrain) cir.getReturnValue();
        MapLike<T> map = ops.getMap(input).getOrThrow();
        T encoded = map.get("IsHandcar");
        train.railways$setHandcar(encoded != null
            && Codec.BOOL.parse(ops, encoded).result().orElse(false));
    }
}
