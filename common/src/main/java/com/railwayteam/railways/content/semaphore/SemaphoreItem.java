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

package com.railwayteam.railways.content.semaphore;

import com.railwayteam.railways.util.EntityUtils;
import com.zurrtum.create.catnip.placement.IPlacementHelper;
import com.zurrtum.create.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class SemaphoreItem extends BlockItem {
    public SemaphoreItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        IPlacementHelper placementHelper = PlacementHelpers.get(SemaphoreBlock.placementHelperId);
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return super.place(context);
        }

        BlockPos pos = context.getClickedPos();
        if (!context.replacingClickedOnBlock()) {
            pos = pos.relative(context.getClickedFace().getOpposite());
        }
        BlockState state = level.getBlockState(pos);
        if (!placementHelper.matchesState(state)) {
            return super.place(context);
        }

        double reach = EntityUtils.getReachDistance(player);
        Vec3 eyePos = player.getEyePosition();
        Vec3 endPos = eyePos.add(player.getViewVector(0).scale(reach));
        BlockHitResult ray = level.clip(new ClipContext(
            eyePos,
            endPos,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));

        InteractionResult result = placementHelper.getOffset(player, level, state, pos, ray)
            .placeInWorld(level, this, player, context.getHand());
        return result.consumesAction() ? result : super.place(context);
    }
}
