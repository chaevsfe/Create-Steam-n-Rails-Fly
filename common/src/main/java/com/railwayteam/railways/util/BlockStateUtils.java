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

package com.railwayteam.railways.util;

import com.zurrtum.create.content.trains.track.TrackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;

import static com.zurrtum.create.content.trains.track.TrackBlock.HAS_BE;
import static com.zurrtum.create.content.trains.track.TrackBlock.SHAPE;
import static com.zurrtum.create.foundation.block.ProperWaterloggedBlock.WATERLOGGED;


public class BlockStateUtils {
    /**
     * @param block an instance of TrackBlock
     * @param state reference BlockState
     * @return block with state applied to it
     */
    public static BlockState trackWith(TrackBlock block, BlockState state) {
        return block.defaultBlockState()
            .setValue(SHAPE, state.getValue(SHAPE))
            .setValue(HAS_BE, state.getValue(HAS_BE))
            .setValue(WATERLOGGED, state.getValue(WATERLOGGED));
    }

    public static SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        return com.railwayteam.railways.util.fabric.BlockStateUtilsImpl.getSoundType(state, level, pos, entity);
    }

    private static final Map<Block, DyeColor> WOOL_MAP = new HashMap<>();
    private static final Map<DyeColor, Block> WOOL_MAP_REVERSE = new HashMap<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            Block wool = Blocks.WOOL.pick(color);
            WOOL_MAP.put(wool, color);
            WOOL_MAP_REVERSE.put(color, wool);
        }
    }

    public static DyeColor getWoolColor(Block block) {
        return WOOL_MAP.getOrDefault(block, DyeColor.WHITE);
    }

    public static Block getWoolBlock(DyeColor color) {
        return WOOL_MAP_REVERSE.getOrDefault(color, Blocks.WOOL.white());
    }

    public static BlockState blockWithProperties(Block blockSource, BlockState propertySource) {
        BlockState state = blockSource.defaultBlockState();
        return blockWithProperties(state, propertySource);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static BlockState blockWithProperties(BlockState blockSource, BlockState propertySource) {
        for (Property property : propertySource.getProperties()) {
            if (blockSource.hasProperty(property)) {
                blockSource = blockSource.setValue(property, propertySource.getValue(property));
            }
        }
        return blockSource;
    }
}
