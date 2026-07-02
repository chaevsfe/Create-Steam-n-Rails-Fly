package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.coupling.coupler.TrackCoupler;
import com.railwayteam.railways.mixin_interfaces.IOccupiedCouplers;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainCoupler implements IOccupiedCouplers {
    @Shadow
    public TrackGraph graph;

    @Shadow
    public boolean updateSignalBlocks;

    @Unique
    private final Set<UUID> railways$occupiedCouplers = new HashSet<>();

    @Override
    public Set<UUID> railways$getOccupiedCouplers() {
        return railways$occupiedCouplers;
    }

    @Inject(method = "earlyTick", at = @At("HEAD"))
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

    @Inject(
        method = "lambda$collectInitiallyOccupiedSignalBlocks$28",
        at = @At("HEAD"),
        cancellable = true
    )
    private void railways$reAddOccupiedCouplers(MutableObject<UUID> prevGroup, Double distance,
                                                Pair<TrackEdgePoint, Couple<TrackNode>> couple,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (couple.getFirst() instanceof TrackCoupler coupler) {
            railways$occupiedCouplers.add(coupler.getId());
            cir.setReturnValue(false);
        }
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
