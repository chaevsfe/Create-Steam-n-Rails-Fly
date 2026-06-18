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

package com.railwayteam.railways.content.custom_bogeys.blocks.base;

import com.railwayteam.railways.content.custom_bogeys.blocks.base.be.CRBogeyBlockEntity;
import com.railwayteam.railways.registry.CRBlockEntities;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CRBogeyBlock extends AbstractBogeyBlock<CRBogeyBlockEntity>
	implements IBE<CRBogeyBlockEntity>, ProperWaterloggedBlock, SpecialBlockItemRequirement {

	private final BogeyStyle defaultStyle;

	protected CRBogeyBlock(Properties props, BogeyStyle defaultStyle, BogeySize size) {
		super(props, size);
		this.defaultStyle = defaultStyle;
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}
	public Identifier getTrackType(BogeyStyle style) {
		return CRTrackMaterials.CRTrackType.STANDARD;
	}
	public double getWheelPointSpacing() {
		return 2;
	}
	public double getWheelRadius() {
		return 6.5 / 16d;
	}
	public Vec3 getConnectorAnchorOffset() {
		return new Vec3(0, 7 / 32f, 1);
	}
	public BogeyStyle getDefaultStyle() {
		return defaultStyle;
	}

	public @NotNull ItemStack getCloneItemStack(@NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull BlockState state) {
		return Blocks.ANDESITE.asItem().getDefaultInstance();
	}
	public Class<CRBogeyBlockEntity> getBlockEntityClass() {
		return CRBogeyBlockEntity.class;
	}
	public BlockEntityType<? extends CRBogeyBlockEntity> getBlockEntityType() {
		return CRBlockEntities.BOGEY.get();
	}
}
