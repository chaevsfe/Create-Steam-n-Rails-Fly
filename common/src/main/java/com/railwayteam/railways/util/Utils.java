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

package com.railwayteam.railways.util;

import com.railwayteam.railways.Railways;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Locale;

public class Utils {
	public static Path configDir() {
		return com.railwayteam.railways.util.fabric.UtilsImpl.configDir();
	}

	public static Path modsDir() {
		return com.railwayteam.railways.util.fabric.UtilsImpl.modsDir();
	}

	public static boolean isDevEnv() {
		return com.railwayteam.railways.util.fabric.UtilsImpl.isDevEnv();
	}

	public static boolean isEnvVarTrue(String name) {
		try {
			String result = System.getenv(name);
			return result != null && result.toLowerCase(Locale.ROOT).equals("true");
		} catch (SecurityException e) {
			Railways.LOGGER.warn("Caught a security exception while trying to access environment variable `{}`.", name);
			return false;
		}
	}

	public static @Nullable String getEnvVar(String name) {
		try {
			String result = System.getenv(name);
			return result != null && !result.isEmpty() ? result : null;
		} catch (SecurityException e) {
			Railways.LOGGER.warn("Caught a security exception while trying to access environment variable `{}`.", name);
			return null;
		}
	}

	public static void sendHonkPacket(Level level, Train train, boolean isHonk) {
		com.railwayteam.railways.util.fabric.UtilsImpl.sendHonkPacket(level, train, isHonk);
	}

	public static void postChunkEventClient(LevelChunk chunk, boolean load) {
		com.railwayteam.railways.util.fabric.UtilsImpl.postChunkEventClient(chunk, load);
	}
}
