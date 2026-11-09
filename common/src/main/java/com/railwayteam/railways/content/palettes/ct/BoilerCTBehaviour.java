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

package com.railwayteam.railways.content.palettes.ct;

import com.railwayteam.railways.content.palettes.boiler.BoilerBlock;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;

public class BoilerCTBehaviour extends ConnectedTextureBehaviour.Base {
    private final CTSpriteShiftEntry shift;

    public BoilerCTBehaviour(CTSpriteShiftEntry shift) {
        this.shift = shift;
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, TextureAtlasSprite sprite) {
        return shift;
    }

    @Override
    protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
        Axis axis = state.getValue(BoilerBlock.HORIZONTAL_AXIS);
        if (face.getAxis() == axis)
            return super.getRightDirection(reader, pos, state, face);
        return Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader,
                              BlockPos pos, BlockPos otherPos, Direction face) {
        if (!super.connectsTo(state, other, reader, pos, otherPos, face))
            return false;
        return other.getValue(BoilerBlock.HORIZONTAL_AXIS) == state.getValue(BoilerBlock.HORIZONTAL_AXIS)
                && other.getValue(BoilerBlock.RAISED) == state.getValue(BoilerBlock.RAISED);
    }
}
