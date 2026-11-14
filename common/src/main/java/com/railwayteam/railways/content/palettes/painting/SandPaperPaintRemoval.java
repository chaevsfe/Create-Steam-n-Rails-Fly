/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

package com.railwayteam.railways.content.palettes.painting;

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.zurrtum.create.AllSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import org.jetbrains.annotations.Nullable;

public final class SandPaperPaintRemoval {
    private SandPaperPaintRemoval() {}

    public static @Nullable InteractionResult strip(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        RepaintingTarget target = RepaintingTarget.get(level, pos, level.getBlockState(pos));
        if (target == null || target.getColor().isNetherite()) return null;

        Player player = context.getPlayer();
        if (!level.isClientSide()) {
            if (!target.repaint(PalettesColor.NETHERITE)) return InteractionResult.FAIL;
            if (player != null) context.getItemInHand().hurtAndBreak(1, player, context.getHand());
        }

        AllSoundEvents.SANDING_LONG.play(level, player, pos, 1, 1 + (level.getRandom().nextFloat() * 0.5f - 1f) / 5f);
        level.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, pos, 0);
        return InteractionResult.SUCCESS;
    }
}
