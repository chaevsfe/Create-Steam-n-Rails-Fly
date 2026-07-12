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

package com.railwayteam.railways.content.buffer.headstock.fabric;

import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CopycatHeadstockBarsModel extends WrapperBlockStateModel {
    public CopycatHeadstockBarsModel(BlockState state, BlockStateModel.UnbakedRoot unbaked) {
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
        model.collectParts(random, parts);
    }

    public void addMaterialParts(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState material,
        RandomSource random,
        Predicate<Direction> shouldUncull,
        List<BlockStateModelPart> output
    ) {
        BlockStateModel materialModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(material);
        List<BlockStateModelPart> materialParts = new ArrayList<>();
        WrapperBlockStateModel.addPartsWithInfo(materialModel, world, pos, material, random, materialParts);

        addRetexturedParts(materialModel, materialParts, random, shouldUncull, output);
    }

    public List<BakedQuad> getMaterialItemQuads(BlockState material, RandomSource random) {
        BlockStateModel materialModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(material);
        List<BlockStateModelPart> materialParts = new ArrayList<>();
        materialModel.collectParts(random, materialParts);

        List<BlockStateModelPart> output = new ArrayList<>();
        addRetexturedParts(materialModel, materialParts, random, direction -> true, output);

        List<BakedQuad> quads = new ArrayList<>();
        for (BlockStateModelPart part : output)
            quads.addAll(part.getQuads(null));
        return List.copyOf(quads);
    }

    private void addRetexturedParts(
        BlockStateModel materialModel,
        List<BlockStateModelPart> materialParts,
        RandomSource random,
        Predicate<Direction> shouldUncull,
        List<BlockStateModelPart> output
    ) {

        Material.Baked particle = materialModel.particleMaterial();
        TextureAtlasSprite mainSprite = particle.sprite();
        TextureAtlasSprite topSprite = findTopSprite(materialParts, mainSprite);

        List<BlockStateModelPart> templateParts = new ArrayList<>();
        model.collectParts(random, templateParts);
        for (BlockStateModelPart templatePart : templateParts) {
            QuadCollection.Builder builder = new QuadCollection.Builder();
            for (BakedQuad quad : templatePart.getQuads(null))
                builder.addUnculledFace(retexture(quad, mainSprite));

            for (Direction direction : Direction.values()) {
                TextureAtlasSprite target = direction.getAxis() == Direction.Axis.Y ? topSprite : mainSprite;
                for (BakedQuad quad : templatePart.getQuads(direction)) {
                    BakedQuad retextured = retexture(quad, target);
                    if (shouldUncull.test(direction))
                        builder.addUnculledFace(retextured);
                    else
                        builder.addCulledFace(direction, retextured);
                }
            }

            output.add(new SimpleModelWrapper(builder.build(), false, particle));
        }
    }

    private static TextureAtlasSprite findTopSprite(
        List<BlockStateModelPart> materialParts,
        TextureAtlasSprite fallback
    ) {
        for (BlockStateModelPart part : materialParts) {
            for (BakedQuad quad : part.getQuads(null)) {
                if (quad.direction() == Direction.UP)
                    return quad.materialInfo().sprite();
            }
        }
        for (BlockStateModelPart part : materialParts) {
            List<BakedQuad> quads = part.getQuads(Direction.UP);
            if (!quads.isEmpty())
                return quads.getFirst().materialInfo().sprite();
        }
        return fallback;
    }

    private static BakedQuad retexture(BakedQuad quad, TextureAtlasSprite target) {
        TextureAtlasSprite source = quad.materialInfo().sprite();
        if (source == target)
            return quad;

        BakedQuad result = new BakedQuad(
            quad.position0(),
            quad.position1(),
            quad.position2(),
            quad.position3(),
            BakedModelHelper.calcSpriteUv(quad.packedUV0(), source, target),
            BakedModelHelper.calcSpriteUv(quad.packedUV1(), source, target),
            BakedModelHelper.calcSpriteUv(quad.packedUV2(), source, target),
            BakedModelHelper.calcSpriteUv(quad.packedUV3(), source, target),
            quad.direction(),
            quad.materialInfo()
        );
        BakedModelHelper.setNormals(result, quad);
        return result;
    }
}
