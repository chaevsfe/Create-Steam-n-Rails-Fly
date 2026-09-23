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
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.render.SuperBufferFactory;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.SegmentAngles;
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
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.NARROW_GAUGE;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.WIDE_GAUGE;
import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedU;
import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedV;

/** Utilities shared by the block-entity and Flywheel track-casing render paths. */
public abstract class CasingRenderUtils {
    private static final Map<Pair<PartialModel, Block>, BlockStateModel> RETEXTURED_MODELS = new HashMap<>();
    private static final Map<Pair<PartialModel, Block>, Model> INSTANCED_MODELS = new HashMap<>();
    private static final Map<Pair<PartialModel, Block>, SuperByteBuffer> CASING_BUFFERS = new HashMap<>();

    public static void clearModelCache() {
        RETEXTURED_MODELS.clear();
        INSTANCED_MODELS.clear();
        CASING_BUFFERS.clear();
        CRBlockPartials.registerCasingSpecs();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.levelRenderer != null) {
            minecraft.levelRenderer.invalidateCompiledGeometry(
                minecraft.level,
                minecraft.options,
                minecraft.gameRenderer.mainCamera(),
                minecraft.getBlockColors()
            );
        }
    }

    /**
     * Builds a 26.2 {@link BlockStateModel} which keeps the partial's geometry and culling,
     * while taking the casing block's texture/material information.
     */
    public static BlockStateModel reTexture(PartialModel model, Block block) {
        Pair<PartialModel, Block> key = Pair.of(model, block);
        return RETEXTURED_MODELS.computeIfAbsent(key, ignored -> {
            BlockStateModel casingModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet()
                .get(block.defaultBlockState());
            return new RetexturedBlockStateModel(model.get(), casingModel);
        });
    }

    public static List<Vec3> casingPositions(BezierConnection connection) {
        List<Vec3> positions = new ArrayList<>();
        List<int[]> takenPositions = new ArrayList<>();
        Identifier trackType = CRTrackMaterials.getType(connection.getMaterial());

        for (BezierConnection.Segment segment : connection) {
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
                int x = Mth.floor(curPos.x);
                int z = Mth.floor(curPos.z);
                if (takenPositions.stream().noneMatch(pos -> pos[0] == x && pos[1] == z)) {
                    takenPositions.add(new int[]{x, z});
                    positions.add(new Vec3(x, curPos.y - (3 / 16d), z));
                }
                curPos = curPos.add(stepVec);
            }
        }

        return List.copyOf(positions);
    }

    public static TransformedInstance makeCasingInstance(
        PartialModel baseModel,
        Block casingBlock,
        InstancerProvider instancerProvider
    ) {
        Pair<PartialModel, Block> key = Pair.of(baseModel, casingBlock);
        Model model = INSTANCED_MODELS.computeIfAbsent(key, ignored -> new BakedModelBuilder(
            reTexture(baseModel, casingBlock)
        ).materialFunc((renderType, shaded, ambientOcclusion) -> {
            var material = ModelUtil.getMaterial(renderType, shaded, ambientOcclusion);
            if (material == null)
                return null;
            return SimpleMaterial.builderOf(material)
                .light(LightShaders.FLAT)
                .cardinalLightingMode(shaded ? CardinalLightingMode.CHUNK : CardinalLightingMode.OFF)
                .build();
        }).build());

        return instancerProvider.instancer(InstanceTypes.TRANSFORMED, model).createInstance();
    }

    public static List<CasingModel> extractTrackCasings(
        TrackBlockEntity blockEntity,
        Function<BezierConnection, SegmentAngles> segmentFactory
    ) {
        if (!hasCasing(blockEntity))
            return List.of();

        Level level = blockEntity.getLevel();
        BlockPos pos = blockEntity.getBlockPos();
        List<CasingModel> casings = new ArrayList<>();
        Map<CasingStateKey, SuperByteBufferRenderState> states = new HashMap<>();
        TrackCasingLayout.Sink sink = (model, casingBlock, pose, lightPos) -> {
            int light = LightCoordsUtil.getLightCoords(level, lightPos);
            SuperByteBufferRenderState state = states.computeIfAbsent(
                new CasingStateKey(model, casingBlock, light),
                key -> casingBuffer(model, casingBlock).cardinalLighting(level).light(light).extractRenderState()
            );
            casings.add(new CasingModel(state, pose));
        };

        PoseStack ms = new PoseStack();
        TransformStack.of(ms).nudge((int) pos.asLong());
        TrackCasingLayout.straight(blockEntity, pos, ms, sink);
        for (BezierConnection connection : blockEntity.getConnections().values())
            TrackCasingLayout.curve(connection, pos, ms, segmentFactory, sink);
        return casings;
    }

    public static void submitCasings(List<CasingModel> casings, PoseStack matrices, SubmitNodeCollector queue) {
        for (CasingModel casing : casings)
            casing.model.submit(casing.transform, matrices, queue);
    }

    private static boolean hasCasing(TrackBlockEntity blockEntity) {
        if (((IHasTrackCasing) blockEntity).railways$getTrackCasing() != null)
            return true;
        for (BezierConnection connection : blockEntity.getConnections().values()) {
            if (connection.isPrimary() && ((IHasTrackCasing) connection).railways$getTrackCasing() != null)
                return true;
        }
        return false;
    }

    private static SuperByteBuffer casingBuffer(PartialModel model, Block block) {
        return CASING_BUFFERS.computeIfAbsent(
            Pair.of(model, block),
            key -> SuperBufferFactory.getInstance().createForBlock(reTexture(model, block), block.defaultBlockState())
        );
    }

    private record CasingStateKey(PartialModel model, Block block, int light) {
    }

    public record CasingModel(SuperByteBufferRenderState model, Pose transform) {
    }

    private static final class RetexturedBlockStateModel implements BlockStateModel {
        private final BlockStateModel baseModel;
        private final BlockStateModel spriteSourceModel;

        private RetexturedBlockStateModel(BlockStateModel baseModel, BlockStateModel spriteSourceModel) {
            this.baseModel = baseModel;
            this.spriteSourceModel = spriteSourceModel;
        }

        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
            List<BlockStateModelPart> baseParts = new ArrayList<>();
            baseModel.collectParts(random, baseParts);
            Map<Direction, BakedQuad> sourceQuads = findSourceQuads(spriteSourceModel);
            Material.Baked particle = spriteSourceModel.particleMaterial();

            for (BlockStateModelPart basePart : baseParts) {
                QuadCollection.Builder builder = new QuadCollection.Builder();
                for (BakedQuad quad : basePart.getQuads(null))
                    builder.addUnculledFace(copyQuad(quad, sourceQuads.get(null)));
                for (Direction direction : Direction.values()) {
                    BakedQuad source = sourceQuads.getOrDefault(direction, sourceQuads.get(null));
                    for (BakedQuad quad : basePart.getQuads(direction))
                        builder.addCulledFace(direction, copyQuad(quad, source));
                }
                output.add(new SimpleModelWrapper(builder.build(), basePart.useAmbientOcclusion(), particle));
            }
        }

        @Override
        public Material.Baked particleMaterial() {
            return spriteSourceModel.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return spriteSourceModel.materialFlags();
        }
    }

    private static Map<Direction, BakedQuad> findSourceQuads(BlockStateModel model) {
        Map<Direction, BakedQuad> found = new HashMap<>();
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(42), parts);

        for (BlockStateModelPart part : parts) {
            for (Direction direction : Direction.values()) {
                List<BakedQuad> quads = part.getQuads(direction);
                if (!quads.isEmpty())
                    found.putIfAbsent(direction, quads.getFirst());
            }
            List<BakedQuad> unculled = part.getQuads(null);
            if (!unculled.isEmpty())
                found.putIfAbsent(null, unculled.getFirst());
        }
        return found;
    }

    private static BakedQuad copyQuad(BakedQuad baseQuad, BakedQuad sourceQuad) {
        if (sourceQuad == null)
            sourceQuad = baseQuad;

        TextureAtlasSprite baseSprite = baseQuad.materialInfo().sprite();
        MaterialInfo sourceInfo = sourceQuad.materialInfo();
        TextureAtlasSprite targetSprite = sourceInfo.sprite();
        return new BakedQuad(
            baseQuad.position0(),
            baseQuad.position1(),
            baseQuad.position2(),
            baseQuad.position3(),
            transformUv(baseQuad.packedUV0(), baseSprite, targetSprite),
            transformUv(baseQuad.packedUV1(), baseSprite, targetSprite),
            transformUv(baseQuad.packedUV2(), baseSprite, targetSprite),
            transformUv(baseQuad.packedUV3(), baseSprite, targetSprite),
            baseQuad.direction(),
            sourceInfo
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
