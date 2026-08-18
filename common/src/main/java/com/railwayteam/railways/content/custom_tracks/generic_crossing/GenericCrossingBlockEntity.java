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

package com.railwayteam.railways.content.custom_tracks.generic_crossing;

import com.railwayteam.railways.content.custom_tracks.generic_crossing.TrackShapeLookup.GenericCrossingData;
import com.railwayteam.railways.mixin_interfaces.IGenericCrossingTrackBE;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.zurrtum.create.foundation.blockEntity.IMergeableBE;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GenericCrossingBlockEntity extends SmartBlockEntity implements IMergeableBE, IGenericCrossingTrackBE {

    boolean cancelDrops = false;
    private Couple<TrackMaterial> materials = null;

    public GenericCrossingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(100);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || cancelDrops)
            return;
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                new ItemStack(getPrimary().getBlock().asItem()));
        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                new ItemStack(getSecondary().getBlock().asItem()));
    }

    @NotNull
    public TrackMaterial getPrimary() {
        if (materials == null)
            return AllTrackMaterials.ANDESITE;
        return materials.getFirst() == null ? AllTrackMaterials.ANDESITE : materials.getFirst();
    }

    @NotNull
    public TrackMaterial getSecondary() {
        if (materials == null)
            return AllTrackMaterials.ANDESITE;
        return materials.getSecond() == null ? AllTrackMaterials.ANDESITE : materials.getSecond();
    }
    public void accept(BlockEntity other) {
        level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
    }
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {}

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);

        boolean updateMesh = false;
        TrackMaterial primary = TrackMaterial.fromId(Identifier.parse(input.getString("PrimaryMaterial").orElse(AllTrackMaterials.ANDESITE.getId().toString())));
        TrackMaterial secondary = TrackMaterial.fromId(Identifier.parse(input.getString("SecondaryMaterial").orElse(AllTrackMaterials.ANDESITE.getId().toString())));

        if (primary != getPrimary() || secondary != getSecondary()) updateMesh = true;

        materials = Couple.create(primary, secondary);

        if (clientPacket && updateMesh)
            redraw();
    }

    private void redraw() {
        if (level != null)
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);

        output.putString("PrimaryMaterial", getPrimary().getId().toString());
        output.putString("SecondaryMaterial", getSecondary().getId().toString());
    }
    public @Nullable Pair<TrackMaterial, TrackShape> railways$getFirstCrossingPiece() {
        TrackMaterial primary = getPrimary();
        Couple<TrackShape> unmerged = TrackShapeLookup.getUnmerged(getBlockState().getValue(GenericCrossingBlock.SHAPE));
        if (unmerged == null) return null;

        return Pair.of(primary, unmerged.getFirst());
    }
    public @Nullable Pair<TrackMaterial, TrackShape> railways$getSecondCrossingPiece() {
        TrackMaterial secondary = getSecondary();
        Couple<TrackShape> unmerged = TrackShapeLookup.getUnmerged(getBlockState().getValue(GenericCrossingBlock.SHAPE));
        if (unmerged == null) return null;

        return Pair.of(secondary, unmerged.getSecond());
    }

    public void initFrom(GenericCrossingData crossingData) {
        if (level.isClientSide()) return;
        boolean flip = crossingData.merged().getSecond();
        TrackMaterial primary = flip ? crossingData.overlayMaterial() : crossingData.existingMaterial();
        TrackMaterial secondary = flip ? crossingData.existingMaterial() : crossingData.overlayMaterial();
        materials = Couple.create(primary, secondary);
        notifyUpdate();
    }
}
