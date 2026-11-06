package com.railwayteam.railways.fabric_mixin;

import com.mojang.datafixers.DataFixerBuilder;
import com.railwayteam.railways.base.datafix.schemas.V4534;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.filefix.FileFixerUpper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataFixers.class)
public class MixinDataFixersSchema {
    @Inject(
        method = "addFixers(Lcom/mojang/datafixers/DataFixerBuilder;Lnet/minecraft/util/filefix/FileFixerUpper$Builder;)V",
        at = @At(value = "CONSTANT", args = "intValue=4535")
    )
    private static void railways$addSchema(DataFixerBuilder builder, FileFixerUpper.Builder fileFixer, CallbackInfo ci) {
        V4534.register(builder);
    }
}
