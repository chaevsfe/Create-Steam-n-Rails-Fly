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

import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm.RestorationTarget;
import com.zurrtum.create.infrastructure.packet.c2s.TrainRelocationPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Routes relocation packets for stored shadow trains before Create looks for a live entity. */
@Mixin(value = TrainRelocationPacket.class, priority = 500, remap = false)
public abstract class MixinTrainRelocationPacketShadow {
    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private void railways$restoreShadowTrain(ServerGamePacketListener listener, CallbackInfo ci) {
        if (!(listener instanceof ServerGamePacketListenerImpl serverListener)) {
            return;
        }
        TrainRelocationPacket packet = (TrainRelocationPacket) (Object) this;
        RestorationTarget target = new RestorationTarget(
            serverListener.player.level(),
            packet.pos(),
            packet.hoveredBezier(),
            packet.direction(),
            packet.lookAngle()
        );
        ShadowRealm.handleTrainRelocationPacket(serverListener.player, packet.trainId(), target, ci);
    }
}
