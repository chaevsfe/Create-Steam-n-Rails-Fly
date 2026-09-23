/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils;
import com.railwayteam.railways.content.custom_tracks.casing.CasingRenderUtils.CasingModel;
import com.railwayteam.railways.mixin_interfaces.ITrackCasingRenderState;
import com.zurrtum.create.client.content.trains.track.TrackRenderer;
import com.zurrtum.create.client.content.trains.track.TrackRenderer.TrackRenderState;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = TrackRenderer.class, remap = false)
public class MixinTrackRenderer {
    @Inject(
        method = "extractRenderState(Lcom/zurrtum/create/content/trains/track/TrackBlockEntity;Lcom/zurrtum/create/client/content/trains/track/TrackRenderer$TrackRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At("HEAD")
    )
    private void railways$extractCasings(TrackBlockEntity blockEntity, TrackRenderState state, float partialTick,
                                         Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling, CallbackInfo ci) {
        List<CasingModel> casings = CasingRenderUtils.extractTrackCasings(blockEntity, InvokerSegmentAngles::railways$newSegmentAngles);
        if (casings.isEmpty()) {
            ((ITrackCasingRenderState) state).railways$setCasings(null);
            return;
        }

        ((ITrackCasingRenderState) state).railways$setCasings(casings);
        state.blockPos = blockEntity.getBlockPos();
        state.blockEntityType = blockEntity.getType();
    }

    @Inject(
        method = "submit(Lcom/zurrtum/create/client/content/trains/track/TrackRenderer$TrackRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At("TAIL")
    )
    private void railways$submitCasings(TrackRenderState state, PoseStack ms, SubmitNodeCollector queue,
                                        CameraRenderState camera, CallbackInfo ci) {
        List<CasingModel> casings = ((ITrackCasingRenderState) state).railways$getCasings();
        if (casings != null)
            CasingRenderUtils.submitCasings(casings, ms, queue);
    }
}
