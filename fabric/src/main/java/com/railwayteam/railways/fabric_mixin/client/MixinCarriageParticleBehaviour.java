/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.zurrtum.create.client.foundation.entity.behaviour.CarriageParticleBehaviour;
import com.zurrtum.create.content.trains.entity.Carriage;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarriageParticleBehaviour.class)
public class MixinCarriageParticleBehaviour {
    @Unique
    private boolean railways$isHandcar;

    @Inject(method = "tick", at = @At("HEAD"))
    private void railways$checkIfHandcar(CallbackInfo ci) {
        Carriage carriage = ((CarriageParticleBehaviour) (Object) this).entity.getCarriage();
        railways$isHandcar = carriage != null
            && carriage.bogeys.both(b -> b == null || b.getStyle() == CRBogeyStyles.HANDCAR);
    }

    @WrapWithCondition(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    )
    private boolean railways$skipHandcarSmoke(Level level, ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        return !railways$isHandcar || particle != CRBogeyStyles.HANDCAR.smokeParticle;
    }
}
