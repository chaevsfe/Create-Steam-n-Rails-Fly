package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class MountedToolboxDisposeAllPacket implements C2SPacket {
	private final int toolboxCarrierId;

	public MountedToolboxDisposeAllPacket(ConductorEntity toolboxCarrier) {
		this.toolboxCarrierId = toolboxCarrier.getId();
	}

	public MountedToolboxDisposeAllPacket(FriendlyByteBuf buffer) {
		toolboxCarrierId = buffer.readInt();
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(toolboxCarrierId);
	}

	public void handle(ServerPlayer player) {
		Entity entity = player.level().getEntity(toolboxCarrierId);
		if (!(entity instanceof ConductorEntity conductor) || !conductor.isCarryingToolbox())
			return;

		double maxRange = ToolboxHandler.getMaxRange(player);
		if (player.distanceToSqr(conductor) > maxRange * maxRange)
			return;

		MountedToolbox toolbox = conductor.getToolbox();
		if (toolbox == null)
			return;
		if (doDisposal(toolbox, player, conductor))
			ToolboxHandler.syncData(player, com.zurrtum.create.AllSynchedDatas.TOOLBOX.get(player));
	}

	@ExpectPlatform
	public static boolean doDisposal(MountedToolbox toolbox, ServerPlayer player, ConductorEntity conductor) {
		throw new AssertionError();
	}
}
