package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.coupling.coupler.TrackCoupler;
import com.railwayteam.railways.mixin_interfaces.IOccupiedCouplers;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainCoupler implements IOccupiedCouplers {
    @Shadow
    public TrackGraph graph;

    @Shadow
    public boolean updateSignalBlocks;

    @Shadow
    public List<Carriage> carriages;

    @Unique
    private final Set<UUID> railways$occupiedCouplers = new HashSet<>();

    @Override
    public Set<UUID> railways$getOccupiedCouplers() {
        return railways$occupiedCouplers;
    }

    @Inject(
        method = "earlyTick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/trains/entity/Train;addToSignalGroups(Ljava/util/Collection;)V",
            ordinal = 2,
            shift = At.Shift.AFTER
        )
    )
    private void railways$tickOccupiedCouplers(Level level, CallbackInfo ci) {
        if (graph == null || railways$occupiedCouplers.isEmpty())
            return;

        for (UUID uuid : railways$occupiedCouplers) {
            TrackCoupler coupler = graph.getPoint(CREdgePointTypes.COUPLER, uuid);
            if (coupler != null)
                coupler.keepAlive((Train) (Object) this);
        }
    }

    @Inject(method = "frontSignalListener", at = @At("RETURN"), cancellable = true)
    private void railways$frontCouplerListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        TravellingPoint.IEdgePointListener originalListener = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof TrackCoupler coupler) {
                railways$occupiedCouplers.add(coupler.getId());
                return false;
            }
            return originalListener.test(distance, couple);
        });
    }

    @Inject(method = "backSignalListener", at = @At("RETURN"), cancellable = true)
    private void railways$backCouplerListener(CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir) {
        TravellingPoint.IEdgePointListener originalListener = cir.getReturnValue();
        cir.setReturnValue((distance, couple) -> {
            if (couple.getFirst() instanceof TrackCoupler coupler) {
                railways$occupiedCouplers.remove(coupler.getId());
                return false;
            }
            return originalListener.test(distance, couple);
        });
    }

    @Inject(method = "collectInitiallyOccupiedSignalBlocks", at = @At("HEAD"))
    private void railways$clearOccupiedCouplers(CallbackInfo ci) {
        railways$occupiedCouplers.clear();
    }

    /**
     * Re-scan the initially occupied train path for couplers after Create has rebuilt its signal
     * occupancy.  Older ports injected into a numbered compiler-generated lambda here; those
     * names are not stable across Create releases (the same lambda is {@code $1} in 26.2).
     * Keeping the hook on the declared method makes it resilient to lambda renumbering.
     */
    @Inject(method = "collectInitiallyOccupiedSignalBlocks", at = @At("TAIL"))
    private void railways$reAddOccupiedCouplers(CallbackInfo ci) {
        TravellingPoint trailingPoint = carriages.getLast().getTrailingPoint();
        if (trailingPoint.edge == null)
            return;

        TravellingPoint couplerScout = new TravellingPoint(
            trailingPoint.node1,
            trailingPoint.node2,
            trailingPoint.edge,
            trailingPoint.position,
            false
        );
        ((Train) (Object) this).forEachTravellingPointBackwards((point, distance) ->
            couplerScout.travel(
                graph,
                distance,
                couplerScout.follow(point),
                (distanceToPoint, edgePoint) -> {
                    if (edgePoint.getFirst() instanceof TrackCoupler coupler)
                        railways$occupiedCouplers.add(coupler.getId());
                    return false;
                },
                couplerScout.ignoreTurns()
            )
        );
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void railways$writeOccupiedCouplers(ValueOutput output, DimensionPalette dimensions, CallbackInfo ci) {
        output.store("OccupiedCouplers", CreateCodecs.UUID_SET_CODEC, railways$occupiedCouplers);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void railways$readOccupiedCouplers(ValueInput input, Map<UUID, TrackGraph> trackNetworks,
                                                      DimensionPalette dimensions, CallbackInfoReturnable<Train> cir) {
        input.read("OccupiedCouplers", CreateCodecs.UUID_SET_CODEC)
            .ifPresent(occupiedCouplers -> ((IOccupiedCouplers) cir.getReturnValue()).railways$getOccupiedCouplers()
                .addAll(occupiedCouplers));
    }
}
