/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

package com.railwayteam.railways.content.buffer.headstock.fabric;

import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.world.level.block.state.BlockState;

public class CopycatHeadstockModelRegistration {
	private static boolean registered;

	public static void register() {
		if (registered)
			return;
		registered = true;

		ModelLoadingPlugin.register(context -> context.modifyBlockModelBeforeBake().register((model, modifierContext) -> {
			BlockState state = modifierContext.state();
			if (state.getBlock() instanceof CopycatHeadstockBlock)
				return new CopycatHeadstockBlockStateModel(state, model);
			return model;
		}));
	}
}
