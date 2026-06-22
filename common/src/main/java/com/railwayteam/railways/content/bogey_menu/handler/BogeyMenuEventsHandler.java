package com.railwayteam.railways.content.bogey_menu.handler;

import com.railwayteam.railways.annotation.multiloader.MultiLoaderEvent;
import com.railwayteam.railways.content.bogey_menu.BogeyMenuScreen;
import com.railwayteam.railways.registry.CRKeys;
import com.railwayteam.railways.util.EntityUtils;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.GameType;

public class BogeyMenuEventsHandler {
	public static int COOLDOWN = 0;

	@MultiLoaderEvent
	public static void clientTick() {
		if (COOLDOWN > 0 && !CRKeys.BOGEY_MENU.isPressed())
			COOLDOWN--;
	}

	@MultiLoaderEvent
	public static void onKeyInput(int key, boolean pressed) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;
		if (key != CRKeys.BOGEY_MENU.getBoundCode() || !pressed || COOLDOWN > 0)
			return;

		LocalPlayer player = mc.player;
		if (player == null || !EntityUtils.isHoldingItem(player, item -> item == AllBlocks.RAILWAY_CASING.asItem()))
			return;

		ScreenOpener.open(new BogeyMenuScreen());
	}
}
