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

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import com.railwayteam.railways.content.buffer.TrackBuffer;
import com.railwayteam.railways.mixin_interfaces.IBufferBlockCheckableNavigation;
import com.railwayteam.railways.mixin_interfaces.IBufferBlockedTrain;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.entity.Navigation;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.station.GlobalStation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = Navigation.class, remap = false)
public abstract class MixinNavigationBuffer implements IBufferBlockCheckableNavigation {
    @Shadow
    public Train train;

    @Shadow
    public GlobalStation destination;

    @Shadow
    @Final
    private TravellingPoint signalScout;

    @Shadow
    public abstract TravellingPoint.ITrackSelector controlSignalScout();

    @Unique
    private final ThreadLocal<Double> railways$bufferDistance = ThreadLocal.withInitial(() -> Double.MAX_VALUE);

    @Inject(method = "tick", at = @At("HEAD"))
    private void railways$resetBufferDistance(Level level, CallbackInfo ci) {
        railways$bufferDistance.set(Double.MAX_VALUE);
    }

    @Inject(
        method = "lambda$tick$0",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/catnip/data/Pair;getFirst()Ljava/lang/Object;")
    )
    private void railways$storeBufferSlowdown(
        MutableObject<Pair<UUID, Boolean>> trackingCrossSignal,
        double scanDistance,
        MutableDouble crossSignalDistanceTracker,
        double brakingDistanceNoFlicker,
        Double distance,
        Pair<TrackEdgePoint, Couple<TrackNode>> couple,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (couple.getFirst() instanceof TrackBuffer) {
            double bufferedDistance = Math.max(0, distance - TrackBuffer.getBufferRoom(this.train));
            railways$bufferDistance.set(Math.min(railways$bufferDistance.get(), bufferedDistance));
        }
    }

    @Inject(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/trains/entity/Train;burnFuel(Lnet/minecraft/world/level/Level;)V")
    )
    private void railways$applyBufferSlowdown(Level level, CallbackInfo ci, @Local(name = "targetDistance") LocalDoubleRef targetDistance) {
        if (railways$bufferDistance.get() < targetDistance.get())
            targetDistance.set(railways$bufferDistance.get());
        railways$bufferDistance.set(Double.MAX_VALUE);
    }

    @ModifyVariable(
        method = "tick",
        at = @At(value = "NEW", target = "(D)Lorg/apache/commons/lang3/mutable/MutableDouble;", ordinal = 0),
        name = "brakingDistance"
    )
    private double railways$ensureSufficientBufferDistance(double brakingDistance) {
        return brakingDistance + TrackBuffer.getBufferRoom(train);
    }

    @ModifyVariable(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(DDD)D", remap = true),
        name = "brakingDistance"
    )
    private double railways$resetSufficientBufferDistance(double brakingDistance) {
        return brakingDistance - TrackBuffer.getBufferRoom(train);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void railways$respectBuffersWithoutSchedule(Level level, CallbackInfo ci) {
        railways$updateControlsBlockInternal(false, false);
    }

    @Override
    public void railways$updateControlsBlock(boolean forceBackwards) {
        railways$updateControlsBlockInternal(true, forceBackwards);
    }

    @Unique
    private void railways$updateControlsBlockInternal(boolean simulate, boolean forceBackwards) {
        ((IBufferBlockedTrain) train).railways$setControlBlocked(false, forceBackwards);
        if (destination != null)
            return;
        if (!train.manualTick && Mth.equal(train.speed, 0))
            return;
        if (train.graph == null)
            return;

        double acceleration = train.acceleration();
        double brakingDistance = (train.speed * train.speed) / (2 * acceleration);
        boolean currentlyBackwards = train.speed < 0 || forceBackwards;
        double speedMod = currentlyBackwards ? -1 : 1;
        double preDepartureLookAhead = train.getCurrentStation() != null ? 4.5 : 0;

        TravellingPoint leadingPoint = !currentlyBackwards
            ? train.carriages.get(0).getLeadingPoint()
            : train.carriages.get(train.carriages.size() - 1).getTrailingPoint();

        signalScout.node1 = leadingPoint.node1;
        signalScout.node2 = leadingPoint.node2;
        signalScout.edge = leadingPoint.edge;
        signalScout.position = leadingPoint.position;

        double paddedBrakingDistance = brakingDistance + TrackBuffer.getBufferRoom(train);
        double brakingDistanceNoFlicker = paddedBrakingDistance + 3 - (paddedBrakingDistance % 3);
        double scanDistance = Mth.clamp(brakingDistanceNoFlicker, preDepartureLookAhead, 500);

        MutableDouble bufferDistance = new MutableDouble(Double.MAX_VALUE);

        signalScout.travel(
            train.graph, (scanDistance + 50) * speedMod, controlSignalScout(), (distance, couple) -> {
                if (distance > scanDistance)
                    return true;

                if (couple.getFirst() instanceof TrackBuffer) {
                    double bufferedDistance = Math.max(0, distance - TrackBuffer.getBufferRoom(this.train, currentlyBackwards));
                    bufferDistance.setValue(Math.min(bufferDistance.getValue(), bufferedDistance));
                    return true;
                }

                return false;
            }, (distance, edge) -> {
            }
        );

        if (bufferDistance.getValue() >= Double.MAX_VALUE)
            return;

        double targetDistance = bufferDistance.getValue() + 0.25;

        if (targetDistance < 3)
            ((IBufferBlockedTrain) train).railways$setControlBlocked(true, forceBackwards);

        if (simulate)
            return;

        if (targetDistance < 10) {
            double target = train.maxSpeed() * (targetDistance / 10);
            if (target < Math.abs(train.speed)) {
                train.speed += (target - Math.abs(train.speed)) * .5f * Math.signum(train.speed);
                return;
            }
        }

        if (targetDistance <= brakingDistance) {
            train.targetSpeed = 0;
            train.approachTargetSpeed(1);
        }
    }
}
