package com.railwayteam.railways.fabric_mixin;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.railwayteam.railways.Railways;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerAdvancements.class)
public abstract class MixinPlayerAdvancementsRepair {
    private static final String STRAY_VERSION_KEY = "Railways_DataVersion";

    @WrapOperation(
        method = "load",
        at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;")
    )
    @SuppressWarnings({"rawtypes", "unchecked"})
    private DataResult railways$dropStrayVersionKey(Codec codec, DynamicOps ops, Object input, Operation<DataResult> original) {
        if (input instanceof JsonObject root && root.has(STRAY_VERSION_KEY)) {
            root.remove(STRAY_VERSION_KEY);
            Railways.LOGGER.warn("Removed a stray {} entry from a player advancement file written by an earlier Railways build; progress kept", STRAY_VERSION_KEY);
        }
        return original.call(codec, ops, input);
    }
}
