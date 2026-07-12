/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.content.trains.track.TrackRenderer;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.SegmentAngles;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.track.BezierConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

@Mixin(value = SegmentAngles.class, remap = false)
public class MixinSegmentAngles {
    @Shadow
    @Final
    public int length;

    @Shadow
    @Final
    public @NotNull BlockPos[] lightPosition;

    @Shadow
    @Final
    public @NotNull Couple<PoseStack.Pose>[] railTransforms;

    @Shadow
    @Final
    public PoseStack.@NotNull Pose[] tieTransform;

    @ModifyExpressionValue(method = "<init>", at = @At(value = "CONSTANT", args = "doubleValue=0.9649999737739563"))
    private static double railways$modifyRailWidth(double original, @Local(argsOnly = true) BezierConnection bc) {
        if (CRTrackMaterials.getType(bc.getMaterial()) == CRTrackMaterials.CRTrackType.WIDE_GAUGE) {
            return original + 0.5;
        } else if (CRTrackMaterials.getType(bc.getMaterial()) == CRTrackMaterials.CRTrackType.NARROW_GAUGE) {
            return original - (7 / 16D);
        }
        return original;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void railways$makeMonorailSegments(BezierConnection bc, CallbackInfo ci) {
        if (!CRTrackMaterials.CRTrackType.MONORAIL.equals(CRTrackMaterials.getType(bc.getMaterial())) || length == 0)
            return;

        Iterator<BezierConnection.Segment> iterator = bc.iterator();
        BezierConnection.Segment segment = iterator.next();
        Vec3 upNormal = segment.derivative.normalize().cross(segment.normal);
        Vec3 firstGirderOffset = upNormal.scale(8 / 16f);
        Couple<Vec3> previousOffsets = Couple.create(
            segment.position.add(firstGirderOffset),
            segment.position.add(firstGirderOffset).add(upNormal.scale(-10 / 16f))
        );

        int i = 0;
        while (iterator.hasNext()) {
            segment = iterator.next();
            boolean end = segment.index == length;

            Vec3 mainGirder = segment.position;
            upNormal = segment.derivative.normalize().cross(segment.normal);
            firstGirderOffset = upNormal.scale(8 / 16f);
            Vec3 mainTop = segment.position.add(firstGirderOffset);
            Vec3 mainBottom = mainTop.add(upNormal.scale(-10 / 16f));
            Couple<Vec3> offsets = Couple.create(mainTop, mainBottom);

            lightPosition[i] = BlockPos.containing(mainGirder);
            railTransforms[i] = Couple.create(null, null);
            float scale = end ? 2.3f : 2.2f;

            Vec3 currentBeam = offsets.getFirst()
                .add(offsets.getSecond())
                .scale(.5);
            Vec3 previousBeam = previousOffsets.getFirst()
                .add(previousOffsets.getSecond())
                .scale(.5);
            Vec3 beamDiff = currentBeam.subtract(previousBeam);
            Vec3 beamAngles = TrackRenderer.getModelAngles(segment.normal, beamDiff);

            PoseStack poseStack = new PoseStack();
            TransformStack.of(poseStack)
                .translate(previousBeam)
                .rotateY((float) beamAngles.y)
                .rotateX((float) beamAngles.x)
                .rotateZ((float) beamAngles.z)
                .translate(0, 2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 1024f, -1 / 32f);
            tieTransform[i] = poseStack.last();

            for (boolean top : Iterate.trueAndFalse) {
                Vec3 current = offsets.get(top);
                Vec3 previous = previousOffsets.get(top);
                Vec3 diff = current.subtract(previous);
                Vec3 capAngles = TrackRenderer.getModelAngles(segment.normal, diff);

                poseStack = new PoseStack();
                TransformStack.of(poseStack)
                    .translate(previous)
                    .rotateY((float) capAngles.y)
                    .rotateX((float) capAngles.x)
                    .rotateZ((float) capAngles.z)
                    .translate(0, 2 / 16f + (segment.index % 2 == 0 ? 1 : -1) / 2048f - 1 / 1024f, -1 / 32f)
                    .scale(1, 1, (float) diff.length() * scale);
                railTransforms[i].set(top, poseStack.last());
            }

            previousOffsets = offsets;
            i++;
        }
    }
}
