/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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
 */

package com.railwayteam.railways.content.custom_tracks.generic_crossing.fabric;

import com.railwayteam.railways.mixin_interfaces.IGenericCrossingTrackBE;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class GenericCrossingModel extends WrapperBlockStateModel {
    public GenericCrossingModel(BlockState state, BlockStateModel.UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        if (!(world.getBlockEntity(pos) instanceof IGenericCrossingTrackBE crossing)) {
            model.collectParts(random, parts);
            return;
        }

        addPiece(world, pos, crossing.railways$getFirstCrossingPiece(), random, parts);
        addPiece(world, pos, crossing.railways$getSecondCrossingPiece(), random, parts);
    }

    private static void addPiece(
        BlockAndTintGetter world,
        BlockPos pos,
        Pair<TrackMaterial, TrackShape> piece,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        if (piece == null)
            return;

        TrackBlock track = piece.getFirst().getBlock();
        BlockState trackState = track.defaultBlockState().setValue(TrackBlock.SHAPE, piece.getSecond());
        BlockStateModel trackModel = IGenericCrossingTrackBE.getModel(piece);
        WrapperBlockStateModel.addPartsWithInfo(trackModel, world, pos, trackState, random, parts);
    }

    @Override
    public boolean needUpdateTerrainParticle() {
        return true;
    }

    @Override
    public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof IGenericCrossingTrackBE crossing) {
            Pair<TrackMaterial, TrackShape> first = crossing.railways$getFirstCrossingPiece();
            if (first != null)
                return IGenericCrossingTrackBE.getModel(first).particleMaterial();
            Pair<TrackMaterial, TrackShape> second = crossing.railways$getSecondCrossingPiece();
            if (second != null)
                return IGenericCrossingTrackBE.getModel(second).particleMaterial();
        }
        return model.particleMaterial();
    }
}
