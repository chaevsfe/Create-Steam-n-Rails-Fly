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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRSounds;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.foundation.entity.behaviour.CarriageAudioBehaviour;
import com.zurrtum.create.content.trains.entity.Carriage;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CarriageAudioBehaviour.class)
public class MixinCarriageAudioBehaviour {
    @Shadow
    LerpedFloat seatCrossfade;

    @Shadow
    Couple<SoundEvent> bogeySounds;

    @Shadow
    SoundEvent closestBogeySound;

    @Unique
    private boolean railways$isHandcar;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void railways$checkBogeys(CallbackInfo ci) {
        Carriage carriage = ((CarriageAudioBehaviour) (Object) this).entity.getCarriage();
        railways$isHandcar = carriage != null
            && carriage.bogeys.both(b -> b == null || b.getStyle() == CRBogeyStyles.HANDCAR);
        if (carriage != null && carriage.bogeys.both(b -> b == null
            || b.getStyle() == CRBogeyStyles.INVISIBLE
            || b.getStyle() == CRBogeyStyles.INVISIBLE_MONOBOGEY))
            ci.cancel();
    }

    @Inject(method = "submitSharedSoundVolume", at = @At("HEAD"))
    private void railways$initBogeySounds(Minecraft mc, Vec3 location, float volume, CallbackInfo ci) {
        if (bogeySounds != null)
            return;
        Carriage carriage = ((CarriageAudioBehaviour) (Object) this).entity.getCarriage();
        if (carriage == null)
            return;
        bogeySounds = carriage.bogeys.map(b -> b != null && b.getStyle() != null
            ? b.getStyle().soundEvent.get()
            : AllSoundEvents.TRAIN2.getMainEvent());
        closestBogeySound = bogeySounds.getFirst();
    }

    @WrapOperation(
        method = {"tick", "submitSharedSoundVolume"},
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/AllSoundEvents$SoundEntry;getMainEvent()Lnet/minecraft/sounds/SoundEvent;")
    )
    private SoundEvent railways$useCogRumble(AllSoundEvents.SoundEntry instance, Operation<SoundEvent> original) {
        if (railways$isHandcar && instance == AllSoundEvents.TRAIN)
            return CRSounds.HANDCAR_COGS.get();
        return original.call(instance);
    }

    @WrapWithCondition(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/AllSoundEvents$SoundEntry;playAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;FFZ)V")
    )
    private boolean railways$skipHandcarSteam(AllSoundEvents.SoundEntry instance, Level level, Vec3 pos, float volume, float pitch, boolean fade) {
        return !railways$isHandcar || instance != AllSoundEvents.STEAM;
    }

    @WrapWithCondition(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V")
    )
    private boolean railways$skipHandcarSteamRelease(Level level, double x, double y, double z, SoundEvent sound, SoundSource source, float volume, float pitch, boolean distanceDelay) {
        return !railways$isHandcar;
    }

    @ModifyArg(
        method = "finalizeSharedVolume",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/foundation/entity/behaviour/CarriageAudioBehaviour$LoopingSound;setVolume(F)V", ordinal = 0)
    )
    private float railways$handcarNoCrossfade(float volume) {
        if (!railways$isHandcar)
            return volume;
        return (1 - seatCrossfade.getValue() * 0.125f) * volume * 1024;
    }
}
