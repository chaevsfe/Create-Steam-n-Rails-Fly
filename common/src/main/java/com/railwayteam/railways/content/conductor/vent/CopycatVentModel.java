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

package com.railwayteam.railways.content.conductor.vent;

import com.railwayteam.railways.content.conductor.ClientHandler;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CopycatVentModel extends CopycatModel {

    public CopycatVentModel(BlockState state, BlockStateModel.UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    protected void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state,
                                    CopycatBlock block, BlockState material,
                                    RandomSource random, List<BlockStateModelPart> parts) {
        if (ClientHandler.isPlayerMountedOnCamera() || state.getValue(VentBlock.CONDUCTOR_VISIBLE)) {
            // Show the vent grate model itself
            model.collectParts(random, parts);
            return;
        }
        // No material applied — show the hollow-frame copycat indicator (create:block/copycat_base
        // is a transparent-center border texture). The false-state blockstate points to
        // railways:block/copycat_vent (cube_all + cutout_mipped) so model.collectParts gives
        // the frame outline, matching the item model and looking "see-through" in the center.
        if (material.is(AllBlocks.COPYCAT_BASE)) {
            model.collectParts(random, parts);
            return;
        }
        // Show whatever block the copycat is set to
        addModelParts(world, pos, material, random, getModelOf(material), parts);
    }
}
