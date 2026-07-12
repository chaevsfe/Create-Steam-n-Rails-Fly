/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.shadow_realm;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.mixin.AccessorGlobalRailwayManager;
import com.railwayteam.railways.mixin_interfaces.IShadowTrain;
import com.railwayteam.railways.mixin_interfaces.RailwaySavedDataDuck;
import com.railwayteam.railways.util.Utils;
import com.railwayteam.railways.util.packet.ShadowTrainRestorePacket;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.RailwaySavedData;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.infrastructure.packet.s2c.AddTrainPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Development probes for persistence, commands, marker packets, and Create's train stream codec. */
public final class ShadowRealmRuntimeChecks {
    private static boolean registered;
    private static boolean checked;

    private ShadowRealmRuntimeChecks() {
    }

    public static void register() {
        if (registered || !Utils.isDevEnv()) {
            return;
        }
        registered = true;
        ServerLifecycleEvents.SERVER_STARTED.register(ShadowRealmRuntimeChecks::run);
    }

    private static void run(MinecraftServer server) {
        if (checked) {
            return;
        }
        checked = true;

        checkCommands(server);
        checkStorageDuck();
        checkMarkerPacket();
        checkTrainPersistenceAndNetworkCodec(server);
        Railways.LOGGER.info(
            "Shadow Realm storage, commands, persistence, marker packet, and AddTrain train-codec checks passed"
        );
    }

    private static void checkCommands(MinecraftServer server) {
        var dispatcher = server.getCommands().getDispatcher();
        require(dispatcher.findNode(List.of("railways", "shadow_realm")) != null,
            "/railways shadow_realm was not registered");
        var snr = dispatcher.findNode(List.of("snr"));
        require(snr != null && (snr.getRedirect() != null || snr.getChild("shadow_realm") != null),
            "/snr shadow_realm was not registered");
    }

    private static void checkStorageDuck() {
        RailwaySavedData savedData = ((AccessorGlobalRailwayManager) Create.RAILWAYS).railways$getSavedData();
        require(savedData != null, "RailwaySavedData was unavailable after server start");
        RailwaySavedDataDuck shadowData = (RailwaySavedDataDuck) savedData;
        require(shadowData.railway$getShadowTrains() != null, "shadow train map was not initialized");
        require(shadowData.railways$getShadowKeys() != null, "shadow key map was not initialized");
    }

    private static void checkMarkerPacket() {
        UUID id = UUID.fromString("4f0974af-5398-4496-95d2-48adf1292547");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            new ShadowTrainRestorePacket(id).write(buffer);
            require(new ShadowTrainRestorePacket(buffer).trainId().equals(id),
                "shadow restore marker packet UUID round-trip failed");
            require(!buffer.isReadable(), "shadow restore marker packet left unread bytes");
        } finally {
            buffer.release();
        }
    }

    private static void checkTrainPersistenceAndNetworkCodec(MinecraftServer server) {
        UUID id = UUID.fromString("1f1fab18-a36e-4eb2-97ac-32abe070cd87");
        Identifier shadowKey = Railways.asResource("runtime_shadow_probe");
        Train original = new Train(id, null, null, List.of(), List.of(), false, 3);
        ((IShadowTrain) original).railways$setShadow(shadowKey);

        DimensionPalette dimensions = new DimensionPalette();
        var persisted = Train.encode(original, NbtOps.INSTANCE, NbtOps.INSTANCE.empty(), dimensions).getOrThrow();
        Train persistedRoundTrip = Train.decode(NbtOps.INSTANCE, persisted, Map.of(), dimensions);
        require(shadowKey.equals(((IShadowTrain) persistedRoundTrip).railways$getShadowKey()),
            "ShadowKey was not preserved by Train persistence encoding");

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
            Unpooled.buffer(),
            server.registryAccess()
        );
        try {
            AddTrainPacket.CODEC.encode(buffer, new AddTrainPacket(original));
            AddTrainPacket decoded = AddTrainPacket.CODEC.decode(buffer);
            require(decoded.train().id.equals(id), "AddTrainPacket/Train.STREAM_CODEC changed the train UUID");
            require(decoded.train().mapColorIndex == 3,
                "AddTrainPacket/Train.STREAM_CODEC changed basic train metadata");
            require(!buffer.isReadable(), "AddTrainPacket train codec left unread bytes");
        } finally {
            buffer.release();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Shadow Realm runtime check failed: " + message);
        }
    }
}
