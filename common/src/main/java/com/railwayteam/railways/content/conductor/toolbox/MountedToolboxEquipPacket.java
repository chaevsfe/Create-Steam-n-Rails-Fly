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

package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MountedToolboxEquipPacket implements C2SPacket {
	private Integer toolboxCarrierId;
	private final int slot;
	private final int hotbarSlot;

	public MountedToolboxEquipPacket(ConductorEntity toolboxCarrier, int slot, int hotbarSlot) {
		this.toolboxCarrierId = toolboxCarrier.getId();
		this.slot = slot;
		this.hotbarSlot = hotbarSlot;
	}

	public MountedToolboxEquipPacket(FriendlyByteBuf buffer) {
		if (buffer.readBoolean())
			toolboxCarrierId = buffer.readInt();
		slot = buffer.readVarInt();
		hotbarSlot = buffer.readVarInt();
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeBoolean(toolboxCarrierId != null);
		if (toolboxCarrierId != null)
			buffer.writeInt(toolboxCarrierId);
		buffer.writeVarInt(slot);
		buffer.writeVarInt(hotbarSlot);
	}

	public void handle(ServerPlayer player) {
		if (!Inventory.isHotbarSlot(hotbarSlot))
			return;

		CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);

		if (toolboxCarrierId == null) {
			ToolboxHandler.unequip(player, hotbarSlot, false);
			ToolboxHandler.syncData(player, compound);
			return;
		}

		Entity entity = player.level().getEntity(toolboxCarrierId);
		if (!(entity instanceof ConductorEntity conductor) || !conductor.isCarryingToolbox())
			return;

		double maxRange = ToolboxHandler.getMaxRange(player);
		if (player.distanceToSqr(conductor) > maxRange * maxRange)
			return;

		ToolboxHandler.unequip(player, hotbarSlot, false);

		if (slot < 0 || slot >= 8) {
			ToolboxHandler.syncData(player, compound);
			return;
		}

		MountedToolbox toolbox = conductor.getToolbox();
		if (toolbox == null)
			return;

		ItemStack held = player.getInventory().getItem(hotbarSlot);
		if (!held.isEmpty()) {
			ToolboxInventory inv = toolbox.inventory;
			ItemStack filterStack = inv.filters.get(slot);
			if (!ToolboxInventory.canItemsShareCompartment(held, filterStack))
				inv.inLimitedMode($ -> doEquip(player, hotbarSlot, held, inv));
		}

		String key = String.valueOf(hotbarSlot);
		CompoundTag data = new CompoundTag();
		data.putInt("Slot", slot);
		data.putString("EntityUUID", conductor.getUUID().toString());
		data.store("Pos", BlockPos.CODEC, new BlockPos(0, 1000, 0));
		compound.put(key, data);

		toolbox.connectPlayer(slot, player, hotbarSlot);
		ToolboxHandler.syncData(player, compound);
	}

	public static void doEquip(ServerPlayer player, int hotbarSlot, ItemStack held, ToolboxInventory inv) {
		com.railwayteam.railways.content.conductor.toolbox.fabric.MountedToolboxEquipPacketImpl.doEquip(player, hotbarSlot, held, inv);
	}
}
