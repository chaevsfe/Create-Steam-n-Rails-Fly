/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.content.conductor.toolbox.fabric;

import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class MountedToolboxEquipPacketImpl {
    public static void doEquip(ServerPlayer player, int hotbarSlot, ItemStack held, ToolboxInventory inv) {
        ItemStack remaining = held.copy();
        for (int compartment = 0; compartment < 8 && !remaining.isEmpty(); compartment++) {
            int inserted = inv.distributeToCompartment(remaining, compartment, false);
            if (inserted > 0)
                remaining.shrink(inserted);
        }
        if (remaining.getCount() != held.getCount())
            player.getInventory().setItem(hotbarSlot, remaining);
    }
}
