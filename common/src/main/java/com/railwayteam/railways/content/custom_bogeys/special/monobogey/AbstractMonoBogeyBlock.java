
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

package com.railwayteam.railways.content.custom_bogeys.special.monobogey;

import com.google.common.collect.ImmutableList;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class AbstractMonoBogeyBlock<T extends MonoBogeyBlockEntity> extends AbstractBogeyBlock<T> implements IBE<T>, ProperWaterloggedBlock, SpecialBlockItemRequirement {

    public static final BooleanProperty UPSIDE_DOWN = BooleanProperty.create("upside_down");

    public AbstractMonoBogeyBlock(Properties pProperties) {
        super(pProperties, AllBogeySizes.SMALL);
        registerDefaultState(defaultBlockState().setValue(UPSIDE_DOWN, false));
    }
    public BlockState getVersion(BlockState base, boolean upsideDown) {
        if (!base.hasProperty(UPSIDE_DOWN))
            return base;
        return base.setValue(UPSIDE_DOWN, upsideDown);
    }
    public Identifier getTrackType(BogeyStyle style) {
        return CRTrackMaterials.CRTrackType.MONORAIL;
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UPSIDE_DOWN);
        super.createBlockStateDefinition(builder);
    }
    public double getWheelPointSpacing() {
        return 2;
    }
    public double getWheelRadius() {
        return 6 / 16d;
    }
    public Vec3 getConnectorAnchorOffset(boolean upsideDown) {
        return new Vec3(0, upsideDown ? 21 / 32f : 11 / 32f, 32 / 32f);
    }
    public Vec3 getConnectorAnchorOffset() {
        return getConnectorAnchorOffset(false);
    }
    public boolean allowsSingleBogeyCarriage() {
        return true;
    }

    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return Blocks.ANDESITE.asItem().getDefaultInstance();
    }
    public BlockState getRotatedBlockState(BlockState state, Direction targetedFace) {
        return state;
    }
    public boolean canBeUpsideDown() {
        return true;
    }
    public boolean isUpsideDown(BlockState state) {
        return state.hasProperty(UPSIDE_DOWN) && state.getValue(UPSIDE_DOWN);
    }

    private final List<Property<?>> properties_to_copy = ImmutableList.<Property<?>>builder()
        .addAll(super.propertiesToCopy())
        .add(UPSIDE_DOWN)
        .build();
    public List<Property<?>> propertiesToCopy() {
        return properties_to_copy;
    }
}
