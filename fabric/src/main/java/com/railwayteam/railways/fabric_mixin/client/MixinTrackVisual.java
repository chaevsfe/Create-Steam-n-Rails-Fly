/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.track.RailwaysTrackVisualBridge;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.SegmentAngles;
import com.zurrtum.create.client.content.trains.track.TrackVisual;
import com.zurrtum.create.client.flywheel.api.visual.BlockEntityVisual;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractVisual;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackShape;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

import static com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils.casingPositions;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.NARROW_GAUGE;
import static com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType.WIDE_GAUGE;

@Mixin(value = TrackVisual.class, remap = false)
public abstract class MixinTrackVisual extends AbstractVisual implements BlockEntityVisual<TrackBlockEntity>, ShaderLightVisual {
    public MixinTrackVisual(VisualizationContext ctx, Level level, float partialTick) {
        super(ctx, level, partialTick);
    }

    @Shadow
    public abstract void _delete();

    @Shadow
    @Final
    protected TrackBlockEntity blockEntity;

    @Shadow
    @Final
    protected BlockPos visualPos;

    @Shadow
    @Final
    protected BlockPos pos;

    @Unique
    private final List<Pair<TransformedInstance, BlockPos>> railways$casingData = new ArrayList<>();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void railways$onCtor(VisualizationContext context, TrackBlockEntity track, float partialTick, CallbackInfo ci) {
        railways$makeCasingData(true);
    }

    @Inject(method = "update", at = @At(value = "RETURN", ordinal = 0))
    private void railways$updateWithoutConnections(float pt, CallbackInfo ci) {
        this._delete();
        railways$makeCasingData(false);
    }

    @Inject(method = "update", at = @At(value = "RETURN", ordinal = 1))
    private void railways$updateWithConnections(float pt, CallbackInfo ci) {
        railways$makeCasingData(true);
    }

    @Inject(method = "_delete", at = @At("HEAD"))
    private void railways$deleteCasings(CallbackInfo ci) {
        railways$casingData.forEach(data -> data.getFirst().delete());
        railways$casingData.clear();
    }

    @ModifyReturnValue(method = "collectLightSections", at = @At("RETURN"))
    private LongSet railways$collectCasingLightSections(LongSet original) {
        if (original.isEmpty())
            return LongSet.of(SectionPos.asLong(blockEntity.getBlockPos()));

        original.add(SectionPos.asLong(blockEntity.getBlockPos()));
        return original;
    }

    @Unique
    private void railways$makeCasingData(boolean connections) {
        PoseStack ms = new PoseStack();
        TransformStack.of(ms)
            .translate(visualPos)
            .nudge((int) this.pos.asLong());

        railways$makeStraightCasing(ms);

        if (!connections)
            return;

        for (BezierConnection bc : this.blockEntity.getConnections().values())
            railways$makeCurveCasing(ms, bc);
    }

    @Unique
    private void railways$makeStraightCasing(PoseStack ms) {
        Block casingBlock = ((IHasTrackCasing) this.blockEntity).railways$getTrackCasing();
        if (casingBlock == null)
            return;

        BlockState state = blockEntity.getBlockState();
        TrackShape shape = state.getValue(TrackBlock.SHAPE);
        if (!CRBlockPartials.TRACK_CASINGS.containsKey(shape))
            return;

        ms.pushPose();
        if (this.blockEntity.isTilted()) {
            double angle = this.blockEntity.tilt.smoothingAngle.get();
            switch (shape) {
                case ZO -> TransformStack.of(ms).rotateXDegrees((float) -angle);
                case XO -> TransformStack.of(ms).rotateZDegrees((float) angle);
            }
        }

        Identifier trackType = null;
        if (state.getBlock() instanceof TrackBlock trackBlock)
            trackType = CRTrackMaterials.getType(trackBlock.getMaterial());

        CRBlockPartials.TrackCasingSpec spec = CRBlockPartials.TRACK_CASINGS.get(shape);
        if (((IHasTrackCasing) this.blockEntity).railways$isAlternate())
            spec = spec.getNonNullAltSpec(trackType);
        else
            spec = spec.getFor(trackType);

        PartialModel rawCasingModel = spec.model;
        railways$addCasingInstance(ms, rawCasingModel, casingBlock, spec.transform, this.pos);

        for (CRBlockPartials.ModelTransform transform : spec.additionalTransforms) {
            railways$addCasingInstance(
                ms,
                rawCasingModel,
                casingBlock,
                transform,
                this.pos.offset(Mth.floor(transform.x()), Mth.floor(transform.y()), Mth.floor(transform.z()))
            );
        }
        ms.popPose();
    }

    @Unique
    private void railways$makeCurveCasing(PoseStack ms, BezierConnection bc) {
        if (!bc.isPrimary())
            return;

        Block casingBlock = ((IHasTrackCasing) bc).railways$getTrackCasing();
        if (casingBlock == null)
            return;

        int heightDiff = Math.abs(bc.bePositions.get(false).getY() - bc.bePositions.get(true).getY());
        double shiftDown = ((IHasTrackCasing) bc).railways$isAlternate() && heightDiff > 0 ? -0.25 : 0;
        PartialModel casingModel = heightDiff == 0 ? CRBlockPartials.TRACK_CASING_FLAT : CRBlockPartials.TRACK_CASING_FLAT_THICK;

        if (heightDiff / bc.getLength() <= 4 / 30d) {
            for (Vec3 pos : casingPositions(bc)) {
                TransformedInstance instance = CasingRenderUtils.makeCasingInstance(casingModel, casingBlock, instancerProvider());
                instance.setTransform(ms)
                    .translate(0, shiftDown, 0)
                    .translate(pos.x, pos.y, pos.z)
                    .scale(1.001f)
                    .setChanged();
                BlockPos relativePos = BlockPos.containing(this.pos.getX() + pos.x, this.pos.getY() + pos.y, this.pos.getZ() + pos.z);
                railways$casingData.add(Pair.of(instance, relativePos));
            }
            return;
        }

        SegmentAngles segments = bc.getBakedSegments(RailwaysTrackVisualBridge::segmentAngles);
        for (int i = 1; i < segments.length; i++) {
            if (i % 2 == 0)
                continue;

            TransformedInstance casingInstance = CasingRenderUtils.makeCasingInstance(casingModel, casingBlock, instancerProvider());
            casingInstance.setTransform(ms)
                .mul(segments.tieTransform[i])
                .translate(0, (i % 4) * 0.001f, 0)
                .translate(0, shiftDown, 0)
                .scale(1.001f)
                .setChanged();
            railways$casingData.add(Pair.of(casingInstance, segments.lightPosition[i].offset(this.pos)));

            railways$makeCurveRailCasings(ms, bc, casingBlock, casingModel, segments, i, shiftDown);
        }
    }

    @Unique
    private void railways$makeCurveRailCasings(PoseStack ms, BezierConnection bc, Block casingBlock, PartialModel casingModel,
                                               SegmentAngles segments, int segment, double shiftDown) {
        Identifier trackType = CRTrackMaterials.getType(bc.getMaterial());
        if (trackType == WIDE_GAUGE) {
            for (boolean first : Iterate.trueAndFalse) {
                for (boolean inner : Iterate.trueAndFalse) {
                    PoseStack.Pose transform = segments.railTransforms[segment].get(first);
                    TransformedInstance instance = CasingRenderUtils.makeCasingInstance(casingModel, casingBlock, instancerProvider());
                    instance.setTransform(ms)
                        .mul(transform)
                        .translate(0, (segment % 4) * 0.001f, 0)
                        .translate((first ? -(61 / 64d) : -(1 / 32d)) + (inner ? 0 : (first ? 1 : -1)), shiftDown, 0)
                        .setChanged();
                    railways$casingData.add(Pair.of(instance, segments.lightPosition[segment].offset(this.pos)));
                }
            }
            return;
        }

        for (boolean first : Iterate.trueAndFalse) {
            PoseStack.Pose transform = segments.railTransforms[segment].get(first);
            TransformedInstance instance = CasingRenderUtils.makeCasingInstance(casingModel, casingBlock, instancerProvider());
            instance.setTransform(ms)
                .mul(transform)
                .translate(0, (segment % 4) * 0.001f, 0)
                .translate(-0.5 + (trackType == NARROW_GAUGE ? (first ? 0.5 : -0.5) : 0), shiftDown, 0)
                .setChanged();
            railways$casingData.add(Pair.of(instance, segments.lightPosition[segment].offset(this.pos)));
        }
    }

    @Unique
    private void railways$addCasingInstance(PoseStack ms, PartialModel model, Block casingBlock,
                                            CRBlockPartials.ModelTransform transform, BlockPos lightPos) {
        TransformedInstance instance = CasingRenderUtils.makeCasingInstance(model, casingBlock, instancerProvider());
        instance.setTransform(ms)
            .rotateX(transform.rx())
            .rotateY(transform.ry())
            .rotateZ(transform.rz())
            .translate(transform.x(), transform.y(), transform.z())
            .setChanged();
        railways$casingData.add(Pair.of(instance, lightPos));
    }
}
