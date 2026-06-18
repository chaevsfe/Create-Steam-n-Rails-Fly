package com.railwayteam.railways.fabric_mixin.client;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.block.model.BlockElement$Deserializer")
public class BlockElementDeserializerMixin {
    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    private static void railways$allowExtendedBlockModelBounds(JsonObject object, String key, CallbackInfoReturnable<Vector3f> cir) {
        cir.setReturnValue(railways$getVector3f(object, key));
    }

    @Unique
    private static Vector3f railways$getVector3f(JsonObject object, String key) {
        var array = GsonHelper.getAsJsonArray(object, key);
        if (array.size() != 3)
            throw new com.google.gson.JsonParseException("Expected 3 " + key + " values, found: " + array.size());

        return new Vector3f(
            GsonHelper.convertToFloat(array.get(0), key + "[0]"),
            GsonHelper.convertToFloat(array.get(1), key + "[1]"),
            GsonHelper.convertToFloat(array.get(2), key + "[2]")
        );
    }
}
