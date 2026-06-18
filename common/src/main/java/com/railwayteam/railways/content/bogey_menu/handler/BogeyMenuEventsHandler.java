package com.railwayteam.railways.content.bogey_menu.handler;

import com.railwayteam.railways.annotation.multiloader.MultiLoaderEvent;

public class BogeyMenuEventsHandler {
	public static int COOLDOWN = 0;

	@MultiLoaderEvent
	public static void clientTick() {
		if (COOLDOWN > 0)
			COOLDOWN--;
	}

	@MultiLoaderEvent
	public static void onKeyInput(int key, boolean pressed) {
	}
}
