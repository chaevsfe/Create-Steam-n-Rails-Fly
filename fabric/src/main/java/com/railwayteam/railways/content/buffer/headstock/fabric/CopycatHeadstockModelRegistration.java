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

import com.railwayteam.railways.content.buffer.TrackBufferBlock;
import com.railwayteam.railways.content.buffer.fabric.BufferModel;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBarsBlock;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.railwayteam.railways.content.buffer.headstock.HeadstockBlock;
import com.railwayteam.railways.content.buffer.single_deco.GenericDyeableSingleBufferBlock;
import com.railwayteam.railways.content.buffer.single_deco.LinkPinBlock;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlock;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.fabric.GenericCrossingModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CopycatHeadstockModelRegistration {
	private static boolean registered;

	public static void register() {
		if (registered)
			return;
		registered = true;

		ModelLoadingPlugin.register(context -> {
			context.modifyBlockModelBeforeBake().register((model, modifierContext) -> {
				BlockState state = modifierContext.state();
				Block block = state.getBlock();
				if (block instanceof CopycatHeadstockBlock)
					return new CopycatHeadstockBlockStateModel(state, model);
				if (block instanceof CopycatHeadstockBarsBlock)
					return new CopycatHeadstockBarsModel(state, model);
				if (block instanceof GenericCrossingBlock)
					return new GenericCrossingModel(state, model);
				if (isBufferBlock(block))
					return new BufferModel(state, model);
				return model;
			});

			context.modifyItemModelBeforeBake().register((model, modifierContext) -> {
				if (!(model instanceof CuboidItemModelWrapper.Unbaked cuboid))
					return model;
				if (!(BuiltInRegistries.ITEM.getValue(modifierContext.itemId()) instanceof BlockItem blockItem))
					return model;
				if (blockItem.getBlock() instanceof CopycatHeadstockBlock)
					return new CopycatHeadstockBlockStateModel.UnbakedItemModel(cuboid);
				return isBufferBlock(blockItem.getBlock()) ? new BufferModel.UnbakedItemModel(cuboid) : model;
			});
		});
	}

	private static boolean isBufferBlock(Block block) {
		return block instanceof TrackBufferBlock<?>
			|| block instanceof GenericDyeableSingleBufferBlock
			|| block instanceof LinkPinBlock
			|| block instanceof HeadstockBlock;
	}
}
