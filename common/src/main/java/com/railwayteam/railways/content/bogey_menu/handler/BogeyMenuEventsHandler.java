/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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
	private static boolean wasPressed = false;

	@MultiLoaderEvent
	public static void clientTick() {
		boolean pressed = CRKeys.BOGEY_MENU.isPressed();
		if (COOLDOWN > 0 && !pressed)
			COOLDOWN--;
		if (pressed && !wasPressed && CRKeys.BOGEY_MENU.isMouseBound())
			tryOpen();
		wasPressed = pressed;
	}

	@MultiLoaderEvent
	public static void onKeyInput(int key, boolean pressed) {
		if (!CRKeys.BOGEY_MENU.matchesKey(key) || !pressed)
			return;
		tryOpen();
	}

	private static void tryOpen() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.gui.screen() != null)
			return;
		if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
			return;
		if (COOLDOWN > 0)
			return;

		LocalPlayer player = mc.player;
		if (player == null || !EntityUtils.isHoldingItem(player, item -> item == AllBlocks.RAILWAY_CASING.asItem()))
			return;

		ScreenOpener.open(new BogeyMenuScreen());
	}
}
