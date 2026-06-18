/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.conductor;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class ClientHandler {
	public static boolean isPlayerMountedOnCamera() {
		return Minecraft.getInstance().getCameraEntity() instanceof ConductorEntity;
	}

	@Nullable
	public static ConductorEntity getPlayerMountedOnCamera() {
		return Minecraft.getInstance().getCameraEntity() instanceof ConductorEntity conductor ? conductor : null;
	}

	public static boolean isPossessed(ConductorEntity conductor) {
		return getPlayerMountedOnCamera() == conductor;
	}
}
