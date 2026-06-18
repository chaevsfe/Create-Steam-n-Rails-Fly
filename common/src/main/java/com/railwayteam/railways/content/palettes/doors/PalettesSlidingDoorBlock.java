/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.content.palettes.doors;

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.registry.CRBlockSetTypes;
import com.railwayteam.railways.util.EntityUtils;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PalettesSlidingDoorBlock extends SlidingDoorBlock implements IWrenchable {
    public static final BooleanProperty WINDOWED = BooleanProperty.create("windowed");
    public final PalettesColor color;

    public static NonNullFunction<Properties, PalettesSlidingDoorBlock> create(boolean folds, PalettesColor color) {
        return p -> new PalettesSlidingDoorBlock(p, folds, color);
    }

    public PalettesSlidingDoorBlock(Properties properties, boolean folds, PalettesColor color) {
        super(properties, CRBlockSetTypes.LOCOMETAL, folds);
        this.color = color;
        registerDefaultState(defaultBlockState()
            .setValue(WINDOWED, false));
    }
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState newState = state.cycle(WINDOWED);
        world.setBlock(pos, newState, UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (EntityUtils.isHolding(player, stack -> stack.is(AllItems.WRENCH))) {
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            Level level = context.getLevel();
            BlockPos posBelow = context.getClickedPos().below();
            return super.onSneakWrenched(level.getBlockState(posBelow), new UseOnContext(
                level,
                context.getPlayer(),
                context.getHand(),
                context.getItemInHand(),
                new BlockHitResult(
                    context.getClickLocation().add(0, -1, 0),
                    context.getClickedFace(),
                    posBelow,
                    context.isInside()
                )
            ));
        }
        return super.onSneakWrenched(state, context);
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(WINDOWED));
    }
}
