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
import com.railwayteam.railways.content.custom_tracks.casing.TrackCasingLayout;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.track.TrackVisual;
import com.zurrtum.create.client.flywheel.api.visual.BlockEntityVisual;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractVisual;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

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

        TrackCasingLayout.straight(this.blockEntity, this.pos, ms, this::railways$addCasingInstance);

        if (!connections)
            return;

        for (BezierConnection bc : this.blockEntity.getConnections().values())
            TrackCasingLayout.curve(bc, this.pos, ms, InvokerSegmentAngles::railways$newSegmentAngles, this::railways$addCasingInstance);
    }

    @Unique
    private void railways$addCasingInstance(PartialModel model, Block casingBlock, PoseStack.Pose pose, BlockPos lightPos) {
        TransformedInstance instance = CasingRenderUtils.makeCasingInstance(model, casingBlock, instancerProvider());
        instance.setTransform(pose).setChanged();
        railways$casingData.add(Pair.of(instance, lightPos));
    }
}
