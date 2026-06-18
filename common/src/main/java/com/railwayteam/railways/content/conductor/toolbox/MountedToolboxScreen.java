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

package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxScreen;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;

@Environment(EnvType.CLIENT)
public class MountedToolboxScreen {
    public static ToolboxScreen create(
        Minecraft mc,
        MenuType<ToolboxBlockEntity> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        int conductorId = extraData.readVarInt();
        CompoundTagReader nbt = new CompoundTagReader(extraData);
        if (mc.level == null)
            return null;
        Entity entity = mc.level.getEntity(conductorId);
        if (!(entity instanceof ConductorEntity conductor)) {
            Railways.LOGGER.error("Conductor with ID not found: {}", conductorId);
            return null;
        }
        MountedToolbox toolbox = conductor.getOrCreateToolboxHolder();
        toolbox.read(nbt.read(), true);
        return new ToolboxScreen(new MountedToolboxContainer(syncId, inventory, toolbox), inventory, title);
    }

    private record CompoundTagReader(RegistryFriendlyByteBuf buffer) {
        net.minecraft.nbt.CompoundTag read() {
            return buffer.readNbt();
        }
    }
}
