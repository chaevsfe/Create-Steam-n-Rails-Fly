/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.zurrtum.create.client.AllHandle;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.packet.s2c.AddTrainPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Converts the marked native AddTrainPacket into a relocation-only client snapshot. */
@Mixin(value = AllHandle.class, remap = false)
public abstract class MixinAllHandleShadowTrain {
    @Inject(method = "onAddTrain", at = @At("HEAD"), cancellable = true)
    private void railways$beginShadowRestore(AddTrainPacket packet, CallbackInfo ci) {
        Train train = packet.train();
        if (!train.id.equals(ShadowRealm.clientPendingShadowTrainId)) {
            return;
        }

        ci.cancel();
        ShadowRealm.clientPendingShadowTrainId = null;
        ShadowRealm.clientShadowRestoringTrain = train;

        LocalPlayer player = Minecraft.getInstance().player;
        AccessorTrainRelocatorClient.railways$setRelocatingTrain(ShadowRealm.MARKER);
        AccessorTrainRelocatorClient.railways$setRelocatingOrigin(
            player == null ? Vec3.ZERO : player.position()
        );
        AccessorTrainRelocatorClient.railways$setRelocatingEntityId(-1);
        ShadowRealm.LOGGER.info("Shadow Realm client restore snapshot accepted for train {}", train.id);
    }
}
