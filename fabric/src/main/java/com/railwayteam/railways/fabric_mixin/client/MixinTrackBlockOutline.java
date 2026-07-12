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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.custom_tracks.monorail.CustomTrackBlockOutline;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = TrackBlockOutline.class, remap = false)
public class MixinTrackBlockOutline {
    /**
     * Create Fly raycasts every Bezier segment with the standard track outline.  Replace that
     * outline before its bounds are calculated so tall monorail curves can be targeted across
     * their complete beam rather than only at rail height.
     */
    @ModifyExpressionValue(
        method = "pickCurves",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/catnip/math/VoxelShaper;get(Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;"
        )
    )
    private static VoxelShape railways$pickCurveWithMaterialShape(
        VoxelShape original,
        @Local BezierConnection connection
    ) {
        return CustomTrackBlockOutline.convert(original, connection.getMaterial());
    }

    /**
     * The selected Bezier segment is rendered with a second hard-coded standard outline.  Resolve
     * the connection from Create's selection record so the visible outline matches the raycast.
     */
    @ModifyArg(
        method = "drawCurveSelection",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/client/content/trains/track/TrackBlockOutline;submitShape(Lnet/minecraft/world/phys/shapes/VoxelShape;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IF)V"
        ),
        index = 0
    )
    private static VoxelShape railways$drawCurveWithMaterialShape(VoxelShape original) {
        TrackBlockOutline.BezierPointSelection selection = TrackBlockOutline.result;
        if (selection == null)
            return original;

        Map<BlockPos, BezierConnection> connections = selection.blockEntity().getConnections();
        if (connections == null)
            return original;

        BezierConnection connection = connections.get(selection.loc().curveTarget());
        return connection == null
            ? original
            : CustomTrackBlockOutline.convert(original, connection.getMaterial());
    }

    /**
     * Create Fly's custom selection renderer dispatches only on TrackShape and consequently never
     * asks a custom TrackBlock for its actual VoxelShape.  Draw the material-aware block shape for
     * monorail and narrow-gauge tracks while preserving Create's colours and alpha animation.
     */
    @Inject(method = "drawCustomBlockSelection", at = @At("HEAD"), cancellable = true)
    private static void railways$drawMaterialBlockShape(
        Minecraft minecraft,
        BlockPos pos,
        float partialTicks,
        SubmitNodeCollector queue,
        PoseStack poseStack,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (minecraft.level == null || minecraft.player == null)
            return;

        BlockState state = minecraft.level.getBlockState(pos);
        if (!(state.getBlock() instanceof TrackBlock trackBlock))
            return;

        Identifier trackType = CRTrackMaterials.getType(trackBlock.getMaterial());
        if (!CRTrackMaterials.CRTrackType.MONORAIL.equals(trackType)
            && !CRTrackMaterials.CRTrackType.NARROW_GAUGE.equals(trackType))
            return;

        if (!minecraft.level.getWorldBorder().isWithinBounds(pos)) {
            cir.setReturnValue(false);
            return;
        }

        TrackShape trackShape = state.getValue(TrackBlock.SHAPE);
        if (trackShape == TrackShape.NONE) {
            cir.setReturnValue(false);
            return;
        }

        int color = TrackBlockOutline.BLACK_COLOR;
        if (minecraft.player.getMainHandItem().is(AllItemTags.TRACKS)) {
            color = TrackBlockOutline.RED_COLOR;
            if (!trackShape.isJunction()) {
                BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
                if (!(blockEntity instanceof TrackBlockEntity trackBlockEntity) || !trackBlockEntity.isTilted())
                    color = TrackBlockOutline.WHITE_COLOR;
            }
        }

        VoxelShape blockShape = state.getShape(minecraft.level, pos);
        VoxelShape materialShape = CustomTrackBlockOutline.convert(blockShape, trackBlock.getMaterial());
        TrackBlockOutline.submitShape(materialShape, poseStack, queue, color, partialTicks);
        cir.setReturnValue(true);
    }
}
