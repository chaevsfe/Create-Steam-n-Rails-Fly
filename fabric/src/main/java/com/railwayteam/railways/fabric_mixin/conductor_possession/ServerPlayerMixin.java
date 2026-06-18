package com.railwayteam.railways.fabric_mixin.conductor_possession;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, priority = 1200)
public class ServerPlayerMixin {
    // snapTo redirect removed — ServerPlayer.tick does not call snapTo in 1.21.11,
    // so the redirect matched 0 targets and crashed at bootstrap.

    @Inject(method = "setCamera", at = @At("HEAD"), cancellable = true)
    private void railways$keepConductorCamera(Entity entityToSpectate, CallbackInfo ci) {
        if (entityToSpectate instanceof ConductorEntity)
            ci.cancel();
    }
}
