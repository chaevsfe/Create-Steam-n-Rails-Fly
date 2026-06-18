/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.custom_tracks.casing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.material.CardinalLightingMode;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.material.LightShaders;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedModelBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.trains.track.BezierConnection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedU;
import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedV;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.NARROW_GAUGE;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.WIDE_GAUGE;

public abstract class CasingRenderUtils {
    private static final Map<Pair<PartialModel, Block>, Model> reTexturedModels = new HashMap<>();

    public static void clearModelCache() {
        reTexturedModels.clear();
        CRBlockPartials.registerCasingSpecs();
        if (Minecraft.getInstance().levelRenderer != null)
            Minecraft.getInstance().levelRenderer.allChanged();
    }

    public static PartialModel reTexture(PartialModel model, Block block) {
        return model;
    }

    public static void renderBezierCasings(PoseStack ms, Level level, PartialModel texturedPartial, BlockState state, VertexConsumer vb, BezierConnection bc) {
    }

    public static List<Vec3> casingPositions(BezierConnection bc) {
        List<Vec3> positions = new ArrayList<>();
        List<int[]> takenPositions = new ArrayList<>();
        Identifier trackType = CRTrackMaterials.getType(bc.getMaterial());

        for (BezierConnection.Segment segment : bc) {
            double factor = 1.3;
            if (trackType == WIDE_GAUGE)
                factor += 0.5;
            else if (trackType == NARROW_GAUGE)
                factor -= 7 / 16d;

            Vec3 pos1 = segment.position.add(segment.normal.scale(factor));
            Vec3 pos2 = segment.position.add(segment.normal.scale(-factor));
            Vec3 stepVec = pos1.vectorTo(pos2).scale(1 / 4d);
            Vec3 curPos = pos1;

            for (int i = 0; i <= 4; i++) {
                int x = (int) Math.floor(curPos.x);
                int z = (int) Math.floor(curPos.z);
                if (takenPositions.stream().noneMatch(pos -> pos[0] == x && pos[1] == z)) {
                    takenPositions.add(new int[]{x, z});
                    positions.add(new Vec3(x, curPos.y - (3 / 16d), z));
                }
                curPos = curPos.add(stepVec);
            }
        }

        return List.copyOf(positions);
    }

    public static TransformedInstance makeCasingInstance(PartialModel baseModel, Block casingBlock, InstancerProvider instancerProvider) {
        Pair<PartialModel, Block> key = Pair.of(baseModel, casingBlock);
        Model model = reTexturedModels.computeIfAbsent(key, ignored -> {
            BlockStateModel casingModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(casingBlock.defaultBlockState());
            SimpleModelWrapper reTextured = reTexture(baseModel.get(), casingModel);

            return new BakedModelBuilder(reTextured)
                .materialFunc((renderType, shaded, ambientOcclusion) -> {
                    var material = ModelUtil.getMaterial(renderType, shaded, ambientOcclusion);
                    if (material == null)
                        return null;
                    return SimpleMaterial.builderOf(material)
                        .light(LightShaders.FLAT)
                        .cardinalLightingMode(shaded ? CardinalLightingMode.CHUNK : CardinalLightingMode.OFF)
                        .build();
                })
                .build();
        });

        return instancerProvider.instancer(InstanceTypes.TRANSFORMED, model).createInstance();
    }

    private static SimpleModelWrapper reTexture(SimpleModelWrapper baseModel, BlockStateModel spriteSourceModel) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        Map<Direction, BakedQuad> sourceQuads = findSourceQuads(spriteSourceModel);
        TextureAtlasSprite particle = spriteSourceModel.particleIcon();

        for (BakedQuad quad : baseModel.quads().getAll()) {
            builder.addUnculledFace(copyQuad(quad, sourceQuads.getOrDefault(quad.direction(), sourceQuads.get(null))));
        }

        return new SimpleModelWrapper(builder.build(), baseModel.useAmbientOcclusion(), particle);
    }

    private static Map<Direction, BakedQuad> findSourceQuads(BlockStateModel model) {
        Map<Direction, BakedQuad> found = new HashMap<>();
        BakedQuad[] unculled = new BakedQuad[1];
        RandomSource random = RandomSource.create(42);

        for (BlockModelPart part : model.collectParts(random)) {
            for (Direction direction : Direction.values()) {
                List<BakedQuad> quads = part.getQuads(direction);
                if (!quads.isEmpty())
                    found.putIfAbsent(direction, quads.get(0));
            }

            List<BakedQuad> quads = part.getQuads(null);
            if (!quads.isEmpty() && unculled[0] == null)
                unculled[0] = quads.get(0);
        }

        if (unculled[0] != null)
            found.put(null, unculled[0]);

        return found;
    }

    private static BakedQuad copyQuad(BakedQuad baseQuad, BakedQuad sourceQuad) {
        if (sourceQuad == null)
            sourceQuad = baseQuad;

        TextureAtlasSprite baseSprite = baseQuad.sprite();
        TextureAtlasSprite targetSprite = sourceQuad.sprite();
        return new BakedQuad(
            baseQuad.position0(),
            baseQuad.position1(),
            baseQuad.position2(),
            baseQuad.position3(),
            transformUv(baseQuad.packedUV0(), baseSprite, targetSprite),
            transformUv(baseQuad.packedUV1(), baseSprite, targetSprite),
            transformUv(baseQuad.packedUV2(), baseSprite, targetSprite),
            transformUv(baseQuad.packedUV3(), baseSprite, targetSprite),
            baseQuad.tintIndex(),
            baseQuad.direction(),
            targetSprite,
            baseQuad.shade(),
            baseQuad.lightEmission()
        );
    }

    private static long transformUv(long packedUv, TextureAtlasSprite baseSprite, TextureAtlasSprite targetSprite) {
        float u = UVPair.unpackU(packedUv);
        float v = UVPair.unpackV(packedUv);
        return UVPair.pack(
            targetSprite.getU(getUnInterpolatedU(baseSprite, u)),
            targetSprite.getV(getUnInterpolatedV(baseSprite, v))
        );
    }
}
