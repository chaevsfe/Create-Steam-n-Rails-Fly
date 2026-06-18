package com.railwayteam.railways.content.buffer;

import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class TrackBufferBlockItem extends BlockItem {
	public static NonNullBiFunction<Block, Item.Properties, ? extends BlockItem> ofType(EdgePointType<?> type) {
		return TrackBufferBlockItem::new;
	}

	public TrackBufferBlockItem(Block block, Properties properties) {
		super(block, properties);
	}
}
