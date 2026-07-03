package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Mixin(value = TrackTargetingBlockItem.class, remap = false)
public class MixinTrackTargetingBlockItem {
    @Shadow
    private EdgePointType<?> type;

    private static final List<TrackShape> railways$acceptableTargetShapes = List.of(
        TrackShape.XO,
        TrackShape.ZO,
        TrackShape.PD,
        TrackShape.ND,
        TrackShape.AN,
        TrackShape.AS,
        TrackShape.AE,
        TrackShape.AW
    );

    @ModifyArg(
        method = "useOn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;closerThan(Lnet/minecraft/core/Vec3i;D)Z"),
        index = 1,
        remap = true
    )
    private double railways$switchPlacementRange(double original) {
        return type == CREdgePointTypes.SWITCH ? CRConfigs.server().switchPlacementRange.get() : original;
    }

    @Inject(
        method = "withGraphLocation",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/trains/track/ITrackBlock;getTrackAxes(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/List;",
            remap = true
        ),
        cancellable = true
    )
    private static void railways$validateSwitchTarget(Level level, BlockPos pos, boolean front,
                                                      BezierTrackPointLocation targetBezier, EdgePointType<?> type,
                                                      BiConsumer<TrackTargetingBlockItem.OverlapResult, TrackGraphLocation> callback,
                                                      CallbackInfo ci) {
        if (type != CREdgePointTypes.COUPLER && type != CREdgePointTypes.SWITCH)
            return;

        TrackTargetingBlockItem.OverlapResult notStraight = TrackTargetingBlockItem.OverlapResult.valueOf("NOT_STRAIGHT");
        if (targetBezier != null) {
            callback.accept(notStraight, null);
            ci.cancel();
            return;
        }

        TrackShape shape = level.getBlockState(pos).getValue(TrackBlock.SHAPE);
        if (!railways$acceptableTargetShapes.contains(shape)
            || (type == CREdgePointTypes.SWITCH && shape.getAxes().stream().anyMatch(v -> v.y > 0))) {
            callback.accept(notStraight, null);
            ci.cancel();
        }
    }

    @Mixin(value = TrackTargetingBlockItem.OverlapResult.class, remap = false)
    public static class MixinOverlapResult {
        @Shadow
        @Final
        @Mutable
        private static TrackTargetingBlockItem.OverlapResult[] $VALUES;

        @Invoker("<init>")
        public static TrackTargetingBlockItem.OverlapResult railways$createType(String internalName, int ordinal, String feedback) {
            throw new AssertionError();
        }

        @Inject(method = "<clinit>", at = @At("TAIL"))
        private static void railways$addTypes(CallbackInfo ci) {
            ArrayList<TrackTargetingBlockItem.OverlapResult> types = new ArrayList<>(List.of($VALUES));
            types.add(railways$createType("NOT_STRAIGHT", $VALUES.length, "track_target.not_straight"));
            $VALUES = types.toArray(TrackTargetingBlockItem.OverlapResult[]::new);
        }
    }
}
