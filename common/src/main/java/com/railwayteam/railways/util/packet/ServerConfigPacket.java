/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.util.packet;

import com.google.gson.JsonObject;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.multiloader.S2CPacket;
import com.zurrtum.create.catnip.config.Builder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.nio.charset.StandardCharsets;

public record ServerConfigPacket(byte[] values) implements S2CPacket {
  public ServerConfigPacket(FriendlyByteBuf buf) {
    this(buf.readByteArray());
  }

  public static ServerConfigPacket of(JsonObject values) {
    return new ServerConfigPacket(Builder.GSON.toJson(values).getBytes(StandardCharsets.UTF_8));
  }

  public void write(FriendlyByteBuf buffer) {
    buffer.writeByteArray(this.values);
  }

  @Environment(EnvType.CLIENT)
  public void handle(Minecraft mc) {
    try {
      CRConfigs.server().reload(Builder.GSON.fromJson(new String(values, StandardCharsets.UTF_8), JsonObject.class));
    } catch (RuntimeException e) {
      Railways.LOGGER.error("Failed to apply the server's Steam 'n' Rails config", e);
    }
  }
}
