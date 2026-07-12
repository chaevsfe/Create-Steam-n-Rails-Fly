package com.railwayteam.railways.fabric_mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.switches.TrackSwitchDebugVisualizer;
import com.railwayteam.railways.util.CustomTrackOverlayRendering;
import com.zurrtum.create.client.content.trains.track.TrackTargetingClient;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TrackTargetingClient.class, remap = false)
public abstract class MixinTrackTargetingClient {
    @Shadow
    static EdgePointType<?> lastType;

    @Shadow
    static BlockPos lastHovered;

    @Shadow
    static boolean lastDirection;

    @Shadow
    static BezierTrackPointLocation lastHoveredBezierSegment;

    @Shadow
    static TrackTargetingBlockItem.OverlapResult lastResult;

    @Shadow
    static TrackGraphLocation lastLocation;

    @Inject(method = "clientTick", at = @At("HEAD"))
    private static void railways$tickSwitchHints(Minecraft mc, CallbackInfo ci) {
        TrackSwitchDebugVisualizer.visualizePotentialLocations();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private static void railways$renderCustomOverlay(Minecraft mc, PoseStack ms, SubmitNodeCollector queue,
                                                     Vec3 camera, CallbackInfo ci) {
        if (lastLocation == null || lastResult == null || lastResult.feedback != null)
            return;
        if (!CustomTrackOverlayRendering.CUSTOM_OVERLAYS.containsKey(lastType) || mc.level == null || lastHovered == null)
            return;

        BlockPos pos = lastHovered;
        Direction.AxisDirection direction = lastDirection
            ? Direction.AxisDirection.POSITIVE
            : Direction.AxisDirection.NEGATIVE;

        ms.pushPose();
        TransformStack.of(ms)
            .translate(Vec3.atLowerCornerOf(pos)
                .subtract(camera));
        boolean rendered = CustomTrackOverlayRendering.renderOverlayIfPresent(
            mc.level, pos, direction, lastHoveredBezierSegment, ms, queue, lastType, 1 + 1 / 16f
        );
        ms.popPose();
        if (rendered)
            ci.cancel();
    }
}
