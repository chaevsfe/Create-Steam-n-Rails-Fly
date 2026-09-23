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
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.SegmentAngles;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.function.Function;

import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.NARROW_GAUGE;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.WIDE_GAUGE;

public final class TrackCasingLayout {
    @FunctionalInterface
    public interface Sink {
        void accept(PartialModel model, Block casingBlock, PoseStack.Pose pose, BlockPos lightPos);
    }

    private TrackCasingLayout() {
    }

    public static void straight(TrackBlockEntity blockEntity, BlockPos pos, PoseStack ms, Sink sink) {
        Block casingBlock = ((IHasTrackCasing) blockEntity).railways$getTrackCasing();
        if (casingBlock == null)
            return;

        BlockState state = blockEntity.getBlockState();
        TrackShape shape = state.getValue(TrackBlock.SHAPE);
        if (!CRBlockPartials.TRACK_CASINGS.containsKey(shape))
            return;

        ms.pushPose();
        if (blockEntity.isTilted()) {
            double angle = blockEntity.tilt.smoothingAngle.get();
            switch (shape) {
                case ZO -> TransformStack.of(ms).rotateXDegrees((float) -angle);
                case XO -> TransformStack.of(ms).rotateZDegrees((float) angle);
            }
        }

        Identifier trackType = null;
        if (state.getBlock() instanceof TrackBlock trackBlock)
            trackType = CRTrackMaterials.getType(trackBlock.getMaterial());

        CRBlockPartials.TrackCasingSpec spec = CRBlockPartials.TRACK_CASINGS.get(shape);
        if (((IHasTrackCasing) blockEntity).railways$isAlternate())
            spec = spec.getNonNullAltSpec(trackType);
        else
            spec = spec.getFor(trackType);

        PartialModel model = spec.model;
        straightPart(ms, model, casingBlock, spec.transform, pos, sink);
        for (CRBlockPartials.ModelTransform transform : spec.additionalTransforms) {
            straightPart(ms, model, casingBlock, transform,
                pos.offset(Mth.floor(transform.x()), Mth.floor(transform.y()), Mth.floor(transform.z())), sink);
        }
        ms.popPose();
    }

    public static void curve(BezierConnection bc, BlockPos pos, PoseStack ms,
                             Function<BezierConnection, SegmentAngles> segmentFactory, Sink sink) {
        if (!bc.isPrimary())
            return;

        Block casingBlock = ((IHasTrackCasing) bc).railways$getTrackCasing();
        if (casingBlock == null)
            return;

        int heightDiff = Math.abs(bc.bePositions.get(false).getY() - bc.bePositions.get(true).getY());
        double shiftDown = ((IHasTrackCasing) bc).railways$isAlternate() && heightDiff > 0 ? -0.25 : 0;
        PartialModel model = heightDiff == 0 ? CRBlockPartials.TRACK_CASING_FLAT : CRBlockPartials.TRACK_CASING_FLAT_THICK;

        if (heightDiff / bc.getLength() <= 4 / 30d) {
            for (Vec3 casingPos : CasingRenderUtils.casingPositions(bc)) {
                ms.pushPose();
                TransformStack.of(ms)
                    .translate(0, shiftDown, 0)
                    .translate(casingPos.x, casingPos.y, casingPos.z)
                    .scale(1.001f);
                sink.accept(model, casingBlock, ms.last().copy(),
                    BlockPos.containing(pos.getX() + casingPos.x, pos.getY() + casingPos.y, pos.getZ() + casingPos.z));
                ms.popPose();
            }
            return;
        }

        SegmentAngles segments = bc.getBakedSegments(segmentFactory);
        for (int i = 1; i < segments.length; i++) {
            if (i % 2 == 0)
                continue;

            BlockPos lightPos = segments.lightPosition[i].offset(pos);
            ms.pushPose();
            TransformStack.of(ms)
                .mulPose(segments.tieTransform[i].pose())
                .translate(0, (i % 4) * 0.001f, 0)
                .translate(0, shiftDown, 0)
                .scale(1.001f);
            sink.accept(model, casingBlock, ms.last().copy(), lightPos);
            ms.popPose();

            curveRails(bc, casingBlock, model, segments, i, shiftDown, lightPos, ms, sink);
        }
    }

    private static void curveRails(BezierConnection bc, Block casingBlock, PartialModel model, SegmentAngles segments,
                                   int segment, double shiftDown, BlockPos lightPos, PoseStack ms, Sink sink) {
        Identifier trackType = CRTrackMaterials.getType(bc.getMaterial());
        if (trackType == WIDE_GAUGE) {
            for (boolean first : Iterate.trueAndFalse) {
                for (boolean inner : Iterate.trueAndFalse) {
                    double x = (first ? -(61 / 64d) : -(1 / 32d)) + (inner ? 0 : (first ? 1 : -1));
                    curveRail(ms, segments.railTransforms[segment].get(first), segment, x, shiftDown, model, casingBlock, lightPos, sink);
                }
            }
            return;
        }

        for (boolean first : Iterate.trueAndFalse) {
            double x = -0.5 + (trackType == NARROW_GAUGE ? (first ? 0.5 : -0.5) : 0);
            curveRail(ms, segments.railTransforms[segment].get(first), segment, x, shiftDown, model, casingBlock, lightPos, sink);
        }
    }

    private static void curveRail(PoseStack ms, PoseStack.Pose railTransform, int segment, double x, double shiftDown,
                                  PartialModel model, Block casingBlock, BlockPos lightPos, Sink sink) {
        ms.pushPose();
        TransformStack.of(ms)
            .mulPose(railTransform.pose())
            .translate(0, (segment % 4) * 0.001f, 0)
            .translate(x, shiftDown, 0);
        sink.accept(model, casingBlock, ms.last().copy(), lightPos);
        ms.popPose();
    }

    private static void straightPart(PoseStack ms, PartialModel model, Block casingBlock,
                                     CRBlockPartials.ModelTransform transform, BlockPos lightPos, Sink sink) {
        ms.pushPose();
        TransformStack.of(ms)
            .rotateX(transform.rx())
            .rotateY(transform.ry())
            .rotateZ(transform.rz())
            .translate(transform.x(), transform.y(), transform.z());
        sink.accept(model, casingBlock, ms.last().copy(), lightPos);
        ms.popPose();
    }
}
