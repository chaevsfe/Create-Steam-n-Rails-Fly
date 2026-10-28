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

import com.zurrtum.create.infrastructure.packet.s2c.AddTrainPacket;
import com.zurrtum.create.infrastructure.packet.s2c.HonkReturnPacket;
import com.zurrtum.create.infrastructure.packet.s2c.RemoveTrainPacket;
import com.zurrtum.create.infrastructure.packet.s2c.TrainEditReturnPacket;
import com.zurrtum.create.infrastructure.packet.s2c.TrainHUDControlUpdatePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
    AddTrainPacket.class,
    RemoveTrainPacket.class,
    TrainHUDControlUpdatePacket.class,
    TrainEditReturnPacket.class,
    HonkReturnPacket.class
}, remap = false)
public abstract class MixinTrainPacketsOnClientThread {
    @SuppressWarnings("unchecked")
    @Inject(
        method = "handle(Lnet/minecraft/network/protocol/game/ClientGamePacketListener;)V",
        at = @At("HEAD")
    )
    private void railways$handleOnClientThread(ClientGamePacketListener listener, CallbackInfo ci) {
        if (listener instanceof ClientPacketListener) {
            PacketUtils.ensureRunningOnSameThread(
                (Packet<ClientGamePacketListener>) (Object) this,
                listener,
                Minecraft.getInstance().packetProcessor()
            );
        }
    }
}
