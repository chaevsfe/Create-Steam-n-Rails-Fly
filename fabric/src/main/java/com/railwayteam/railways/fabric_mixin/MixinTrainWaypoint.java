/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.mixin.AccessorNavigation;
import com.railwayteam.railways.mixin_interfaces.IWaypointableNavigation;
import com.zurrtum.create.content.trains.entity.Navigation;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.station.GlobalStation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Advances a waypoint entry when the train crosses its station without stopping its motion. */
@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainWaypoint {
    @Shadow
    public Navigation navigation;

    @Shadow
    public abstract void arriveAt(GlobalStation station);

    @Inject(method = "frontSignalListener", at = @At("RETURN"), cancellable = true)
    private void railways$frontWaypointListener(
        CallbackInfoReturnable<TravellingPoint.IEdgePointListener> cir
    ) {
        TravellingPoint.IEdgePointListener original = cir.getReturnValue();
        cir.setReturnValue((distance, point) -> {
            if (((IWaypointableNavigation) navigation).railways$isWaypointMode()
                && point.getFirst() instanceof GlobalStation station
                && station == navigation.destination) {
                if (!station.canApproachFrom(point.getSecond().getSecond())) {
                    return false;
                }

                navigation.distanceToDestination = 0;
                ((AccessorNavigation) navigation).getCurrentPath().clear();
                arriveAt(station);
                navigation.destination = null;
                return true;
            }

            return original.test(distance, point);
        });
    }
}
