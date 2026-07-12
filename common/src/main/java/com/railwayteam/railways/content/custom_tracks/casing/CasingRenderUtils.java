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
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.render.SuperBufferFactory;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.NARROW_GAUGE;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.WIDE_GAUGE;
import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedU;
import static com.zurrtum.create.client.catnip.render.SpriteShiftEntry.getUnInterpolatedV;

/** Utilities shared by the block-entity and Flywheel track-casing render paths. */
public abstract class CasingRenderUtils {
    private static final Map<Pair<PartialModel, Block>, BlockStateModel> RETEXTURED_MODELS = new HashMap<>();
    private static final Map<Pair<PartialModel, Block>, Model> INSTANCED_MODELS = new HashMap<>();

    public static void clearModelCache() {
        RETEXTURED_MODELS.clear();
        INSTANCED_MODELS.clear();
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

    /**
     * Extracts the curve casing into immutable render data.  Minecraft 26.2 no longer allows
     * block-entity renderers to emit vertices while extracting world state, so extraction and
     * submission have deliberately separate entry points.
     */
    public static BezierCasingRenderState extractBezierCasings(
        Level level,
        BlockStateModel texturedModel,
        BlockState state,
        BezierConnection connection
    ) {
        List<CasingModel> models = new ArrayList<>();
        Map<Integer, SuperByteBufferRenderState> modelsByLight = new HashMap<>();
        int heightDiff = Math.abs(
            connection.bePositions.getFirst().getY() - connection.bePositions.getSecond().getY()
        );
        double shiftDown = connection instanceof IHasTrackCasing casing
            && casing.railways$isAlternate() && heightDiff > 0 ? -0.25 : 0;
        BlockPos blockEntityPos = connection.bePositions.getFirst();

        if (heightDiff / connection.getLength() <= 4 / 30d) {
            for (Vec3 position : casingPositions(connection)) {
                int light = LightCoordsUtil.getLightCoords(
                    level,
                    BlockPos.containing(position).offset(blockEntityPos)
                );
                Pose transform = new Pose();
                transform.translate((float) position.x, (float) (position.y + shiftDown), (float) position.z);
                transform.scale(1.001f, 1.001f, 1.001f);
                models.add(new CasingModel(
                    modelsByLight.computeIfAbsent(light, ignored -> createRenderState(texturedModel, state, level, light)),
                    transform
                ));
            }
            return new BezierCasingRenderState(List.copyOf(models));
        }

        CasingSegmentAngles segments = new CasingSegmentAngles(connection);
        Identifier trackType = CRTrackMaterials.getType(connection.getMaterial());
        for (int i = 1; i < segments.length; i += 2) {
            int light = LightCoordsUtil.getLightCoords(level, segments.lightPosition[i].offset(blockEntityPos));
            float zFightOffset = (i % 4) * 0.001f;

            Pose tie = segments.tieTransform[i].copy();
            tie.translate(0, (float) shiftDown + zFightOffset, 0);
            tie.scale(1.02f, 1.02f, 1.02f);
            SuperByteBufferRenderState litModel = modelsByLight.computeIfAbsent(
                light,
                ignored -> createRenderState(texturedModel, state, level, light)
            );
            models.add(new CasingModel(litModel, tie));

            if (trackType == WIDE_GAUGE) {
                for (boolean first : new boolean[]{true, false}) {
                    for (boolean inner : new boolean[]{true, false}) {
                        Pose rail = segments.railTransforms[i].get(first).copy();
                        float x = (float) ((first ? -(61 / 64d) : -(1 / 32d))
                            + (inner ? 0 : first ? 1 : -1));
                        rail.translate(x, (float) shiftDown + zFightOffset, 0);
                        models.add(new CasingModel(litModel, rail));
                    }
                }
            } else {
                for (boolean first : new boolean[]{true, false}) {
                    Pose rail = segments.railTransforms[i].get(first).copy();
                    float gaugeOffset = trackType == NARROW_GAUGE ? (first ? 0.5f : -0.5f) : 0;
                    rail.translate(-0.5f + gaugeOffset, (float) shiftDown + zFightOffset, 0);
                    models.add(new CasingModel(litModel, rail));
                }
            }
        }
        return new BezierCasingRenderState(List.copyOf(models));
    }

    public static void renderBezierCasings(
        PoseStack matrices,
        SubmitNodeCollector queue,
        BezierCasingRenderState state
    ) {
        state.submit(matrices, queue);
    }

    /** Compatibility bridge for the legacy vertex-consumer renderer. */
    @Deprecated(forRemoval = true)
    public static void renderBezierCasings(
        PoseStack matrices,
        Level level,
        BlockStateModel texturedModel,
        BlockState state,
        VertexConsumer consumer,
        BezierConnection connection
    ) {
        BezierCasingRenderState renderState = extractBezierCasings(level, texturedModel, state, connection);
        Pose parent = matrices.last();
        for (CasingModel casing : renderState.models) {
            Pose combined = parent.copy();
            combined.pose().mul(casing.transform.pose());
            combined.normal().mul(casing.transform.normal());
            casing.model.renderInto(combined, consumer);
        }
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

    private static SuperByteBufferRenderState createRenderState(
        BlockStateModel model,
        BlockState state,
        Level level,
        int light
    ) {
        return SuperBufferFactory.getInstance().createForBlock(model, state)
            .cardinalLighting(level)
            .light(light)
            .extractRenderState();
    }

    public record BezierCasingRenderState(List<CasingModel> models) {
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            for (CasingModel casing : models)
                casing.model.submit(casing.transform, matrices, queue);
        }
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

    /** A casing-local copy of Create's curve transforms; its cache cannot be shared with TrackRenderer. */
    private static final class CasingSegmentAngles {
        private final int length;
        private final Pose[] tieTransform;
        private final Couple<Pose>[] railTransforms;
        private final BlockPos[] lightPosition;

        @SuppressWarnings({"unchecked", "DataFlowIssue"})
        private CasingSegmentAngles(BezierConnection connection) {
            length = connection.getSegmentCount();
            tieTransform = new Pose[length];
            railTransforms = new Couple[length];
            lightPosition = new BlockPos[length];
            if (length == 0)
                return;

            Iterator<BezierConnection.Segment> iterator = connection.iterator();
            BezierConnection.Segment segment = iterator.next();
            Couple<Vec3> previousOffsets = Couple.create(
                segment.position.add(segment.normal.scale(0.965f)),
                segment.position.subtract(segment.normal.scale(0.965f))
            );
            int i = 0;
            while (iterator.hasNext()) {
                segment = iterator.next();
                Couple<Vec3> railOffsets = Couple.create(
                    segment.position.add(segment.normal.scale(0.965f)),
                    segment.position.subtract(segment.normal.scale(0.965f))
                );
                Vec3 railMiddle = railOffsets.getFirst().add(railOffsets.getSecond()).scale(0.5);
                Vec3 previousMiddle = previousOffsets.getFirst().add(previousOffsets.getSecond()).scale(0.5);
                Vec3 tieAngles = modelAngles(segment.normal, railMiddle.subtract(previousMiddle));

                lightPosition[i] = BlockPos.containing(railMiddle);
                railTransforms[i] = Couple.create(null, null);
                Pose tie = new Pose();
                tie.translate((float) previousMiddle.x, (float) previousMiddle.y, (float) previousMiddle.z);
                tie.rotate(Axis.YP.rotation((float) tieAngles.y));
                tie.rotate(Axis.XP.rotation((float) tieAngles.x));
                tie.rotate(Axis.ZP.rotation((float) tieAngles.z));
                tie.translate(-0.5f, -0.125f - 1 / 256f, 0);
                tieTransform[i] = tie;

                float scale = segment.index == length ? 2.2f : 2.1f;
                for (boolean first : new boolean[]{true, false}) {
                    Vec3 rail = railOffsets.get(first);
                    Vec3 previous = previousOffsets.get(first);
                    Vec3 difference = rail.subtract(previous);
                    Vec3 angles = modelAngles(segment.normal, difference);

                    Pose pose = new Pose();
                    pose.translate((float) previous.x, (float) previous.y, (float) previous.z);
                    pose.rotate(Axis.YP.rotation((float) angles.y));
                    pose.rotate(Axis.XP.rotation((float) angles.x));
                    pose.rotate(Axis.ZP.rotation((float) angles.z));
                    pose.translate(0, -0.125f - 1 / 256f, -0.03125f);
                    pose.scale(1, 1, (float) difference.length() * scale);
                    railTransforms[i].set(first, pose);
                }

                previousOffsets = railOffsets;
                i++;
            }
        }

        private static Vec3 modelAngles(Vec3 normal, Vec3 difference) {
            double len = Mth.sqrt((float) (difference.x * difference.x + difference.z * difference.z));
            double yaw = Mth.atan2(difference.x, difference.z);
            double pitch = Mth.atan2(len, difference.y) - Math.PI * 0.5;
            Vec3 yawPitchNormal = new Vec3(0, 1, 0)
                .xRot((float) pitch)
                .yRot((float) yaw);
            double signum = Math.signum(yawPitchNormal.dot(normal));
            if (Math.abs(signum) < 0.5)
                signum = yawPitchNormal.distanceToSqr(normal) < 0.5 ? -1 : 1;
            double dot = difference.cross(normal).normalize().dot(yawPitchNormal);
            double roll = Math.acos(Mth.clamp(dot, -1, 1)) * signum;
            return new Vec3(pitch, yaw, roll);
        }
    }
}
