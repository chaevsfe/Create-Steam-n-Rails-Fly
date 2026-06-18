package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

	@ExpectPlatform
	public static void doEquip(ServerPlayer player, int hotbarSlot, ItemStack held, ToolboxInventory inv) {
		throw new AssertionError();
	}
}
