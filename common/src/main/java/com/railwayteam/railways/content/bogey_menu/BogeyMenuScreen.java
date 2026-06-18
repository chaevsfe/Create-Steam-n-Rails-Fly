/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.bogey_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BogeyMenuScreen extends Screen {
	public BogeyMenuScreen() {
		super(Component.translatable("railways.gui.bogey_menu"));
	}
}
