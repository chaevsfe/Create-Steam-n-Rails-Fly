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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.content.schedule.WaypointDestinationInstruction;
import com.railwayteam.railways.mixin_interfaces.IWaypointableNavigation;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.entity.Navigation;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.station.GlobalStation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.UUID;

/** Keeps scheduled waypoint stations as routing targets without braking the train there. */
@Mixin(value = Navigation.class, remap = false)
public abstract class MixinNavigationWaypoint implements IWaypointableNavigation {
    @Shadow
    public Train train;

    @Override
    public boolean railways$isWaypointMode() {
        if (train.manualTick || train.runtime.paused || train.runtime.completed) {
            return false;
        }
        var schedule = train.runtime.getSchedule();
        return schedule != null
            && train.runtime.currentEntry >= 0
            && train.runtime.currentEntry < schedule.entries.size()
            && schedule.entries.get(train.runtime.currentEntry).instruction
                instanceof WaypointDestinationInstruction;
    }

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lcom/zurrtum/create/content/trains/entity/Navigation;distanceToDestination:D"
        )
    )
    private double railways$keepWaypointCruising(Navigation instance, Operation<Double> original) {
        return railways$isWaypointMode() ? 1000 : original.call(instance);
    }

    @WrapOperation(
        method = "lambda$tick$0",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/trains/station/GlobalStation;canApproachFrom(Lcom/zurrtum/create/content/trains/graph/TrackNode;)Z"
        )
    )
    private boolean railways$scanSignalsPastWaypoint(
        GlobalStation station,
        TrackNode side,
        Operation<Boolean> original
    ) {
        return original.call(station, side) && !railways$isWaypointMode();
    }

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lcom/zurrtum/create/content/trains/entity/Navigation;waitingForSignal:Lcom/zurrtum/create/catnip/data/Pair;"
        ),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "doubleValue=0.25"),
            to = @At(
                value = "INVOKE",
                target = "Lcom/zurrtum/create/content/trains/entity/Train;leaveStation()V"
            )
        )
    )
    private Pair<UUID, Boolean> railways$doNotBrakeAtWaypoint(
        Navigation instance,
        Operation<Pair<UUID, Boolean>> original
    ) {
        return railways$isWaypointMode() ? null : original.call(instance);
    }

    @WrapOperation(
        method = "currentSignalResolved",
        at = @At(
            value = "FIELD",
            opcode = Opcodes.GETFIELD,
            target = "Lcom/zurrtum/create/content/trains/entity/Navigation;distanceToDestination:D"
        )
    )
    private double railways$keepWaypointSignalReservation(
        Navigation instance,
        Operation<Double> original
    ) {
        return railways$isWaypointMode() ? 10 : original.call(instance);
    }
}
