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

package com.railwayteam.railways.content.coupling.coupler;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRBlockEntities;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TrackCouplerBlock extends Block implements IBE<TrackCouplerBlockEntity>, IWrenchable {

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final EnumProperty<TrackCouplerBlockEntity.AllowedOperationMode> MODE = EnumProperty.create("mode", TrackCouplerBlockEntity.AllowedOperationMode.class);

	protected TrackCouplerBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false).setValue(MODE, TrackCouplerBlockEntity.AllowedOperationMode.BOTH));
	}

	@ExpectPlatform
	public static TrackCouplerBlock create(Properties properties) {
		throw new AssertionError();
	}
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(POWERED).add(MODE));
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		boolean hasSignal = pContext.getLevel().hasNeighborSignal(pContext.getClickedPos());
		if (!pContext.getLevel().isClientSide())
			Railways.LOGGER.info("[TrackCouplerBlock {}] placed hasSignal={}", pContext.getClickedPos(), hasSignal);
		return this.defaultBlockState().setValue(POWERED, hasSignal);
	}

	/**
	 * @deprecated call via {@link
	 * BlockStateBase#hasAnalogOutputSignal} whenever possible.
	 * Implementing/overriding is fine.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
		return true;
	}

	/**
	 * @deprecated call via {@link
	 * BlockStateBase#getAnalogOutputSignal} whenever possible.
	 * Implementing/overriding is fine.
	 */
	@SuppressWarnings("deprecation")
	@Deprecated
	public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof TrackCouplerBlockEntity te)
			return te.getTargetAnalogOutput();
		return 0;
	}
	public Class<TrackCouplerBlockEntity> getBlockEntityClass() {
		return TrackCouplerBlockEntity.class;
	}
	public BlockEntityType<? extends TrackCouplerBlockEntity> getBlockEntityType() {
		return CRBlockEntities.TRACK_COUPLER.get();
	}
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock()))
			worldIn.removeBlockEntity(pos);
	}
	@Override
	protected void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, Orientation pOrientation,
								   boolean pIsMoving) {
		if (pLevel.isClientSide())
			return;
		boolean powered = pState.getValue(POWERED);
		boolean hasSignal = pLevel.hasNeighborSignal(pPos);
		Railways.LOGGER.info("[TrackCouplerBlock {}] neighborChanged powered={} hasSignal={} moving={}",
			pPos, powered, hasSignal, pIsMoving);
		if (powered == hasSignal)
			return;
		if (powered) {
			Railways.LOGGER.info("[TrackCouplerBlock {}] scheduling unpower tick", pPos);
			pLevel.scheduleTick(pPos, this, 4);
		} else {
			Railways.LOGGER.info("[TrackCouplerBlock {}] setting powered=true", pPos);
			pLevel.setBlock(pPos, pState.cycle(POWERED), 2);
		}
	}
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRand) {
		boolean powered = pState.getValue(POWERED);
		boolean hasSignal = pLevel.hasNeighborSignal(pPos);
		Railways.LOGGER.info("[TrackCouplerBlock {}] scheduled tick powered={} hasSignal={}", pPos, powered, hasSignal);
		if (powered && !hasSignal)
			pLevel.setBlock(pPos, pState.cycle(POWERED), 2);
	}
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		Level level = context.getLevel();
		if (level.isClientSide())
			return InteractionResult.SUCCESS;
		BlockPos pos = context.getClickedPos();
		BlockState newState = state.cycle(MODE);
		level.setBlock(pos, newState, 3);
		Railways.LOGGER.info("[TrackCouplerBlock {}] wrenched mode {} -> {} powered={}",
			pos, state.getValue(MODE), newState.getValue(MODE), newState.getValue(POWERED));
		Player player = context.getPlayer();
		if (player != null)
			player.displayClientMessage(newState.getValue(MODE).getTranslatedName(), true);
		return InteractionResult.SUCCESS;
	}
}
