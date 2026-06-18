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

package com.railwayteam.railways.content.custom_bogeys.special.invisible;

import com.google.common.collect.ImmutableSet;
import com.railwayteam.railways.registry.CRBlockEntities;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRShapes;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import net.minecraft.resources.Identifier;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;

public class InvisibleBogeyBlock extends AbstractBogeyBlock<InvisibleBogeyBlockEntity>
	implements IBE<InvisibleBogeyBlockEntity>, ProperWaterloggedBlock, SpecialBlockItemRequirement {

	public InvisibleBogeyBlock(Properties props) {
		super(props, AllBogeySizes.SMALL);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
	}
	public Identifier getTrackType(BogeyStyle style) {
		return CRTrackMaterials.CRTrackType.STANDARD;
	}
	public boolean isOnIncompatibleTrack(Carriage carriage, boolean leading) {
		TravellingPoint point = leading ? carriage.getLeadingPoint() : carriage.getTrailingPoint();
		CarriageBogey bogey = leading ? carriage.leadingBogey() : carriage.trailingBogey();
		
		if (point.edge == null)
			return false;
		
		return CRTrackMaterials.getType(point.edge.getTrackMaterial()) != getTrackType(bogey.getStyle())
			&& CRTrackMaterials.getType(point.edge.getTrackMaterial()) != CRTrackType.WIDE_GAUGE
			&& CRTrackMaterials.getType(point.edge.getTrackMaterial()) != CRTrackType.NARROW_GAUGE
			&& CRTrackMaterials.getType(point.edge.getTrackMaterial()) != CRTrackType.MONORAIL;
	}
	public Set<Identifier> getValidPathfindingTypes(BogeyStyle style) {
		return ImmutableSet.of(getTrackType(style), CRTrackType.WIDE_GAUGE, CRTrackType.NARROW_GAUGE, CRTrackType.MONORAIL);
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
		return CRBogeyStyles.INVISIBLE;
	}

	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
		return Blocks.ANDESITE.asItem().getDefaultInstance();
	}
	public Class<InvisibleBogeyBlockEntity> getBlockEntityClass() {
		return InvisibleBogeyBlockEntity.class;
	}
	public BlockEntityType<? extends InvisibleBogeyBlockEntity> getBlockEntityType() {
		return CRBlockEntities.INVISIBLE_BOGEY.get();
	}

	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return CRShapes.INVISIBLE_BOGEY;
	}
}
