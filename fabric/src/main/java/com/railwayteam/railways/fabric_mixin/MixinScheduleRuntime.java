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

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.schedule.WaypointDestinationInstruction;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.schedule.Schedule;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Restores Railway's schedule progression and waypoint prediction semantics. */
@Mixin(value = ScheduleRuntime.class, remap = false)
public abstract class MixinScheduleRuntime {
    @Shadow
    public Schedule schedule;

    @Shadow
    public int currentEntry;

    @Shadow
    public ScheduleRuntime.State state;

    @Shadow
    public boolean isAutoSchedule;

    @Shadow
    public Train train;

    @Shadow
    public abstract void discardSchedule();

    @Inject(method = "tickConditions", at = @At("HEAD"), cancellable = true)
    private void railways$advanceEntriesWithoutConditions(Level level, CallbackInfo ci) {
        if (schedule != null
            && currentEntry >= 0
            && currentEntry < schedule.entries.size()
            && schedule.entries.get(currentEntry).conditions.isEmpty()) {
            state = ScheduleRuntime.State.PRE_TRANSIT;
            currentEntry++;
            ci.cancel();
        }
    }

    @Inject(
        method = "checkEndOfScheduleReached",
        at = @At(
            value = "FIELD",
            target = "Lcom/zurrtum/create/content/trains/schedule/ScheduleRuntime;completed:Z",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void railways$discardCompletedAutoSchedule(CallbackInfoReturnable<Boolean> cir) {
        if (!isAutoSchedule) {
            return;
        }
        Railways.LOGGER.debug(
            "Discarding completed non-looping auto schedule on train {}",
            train.name.getString()
        );
        discardSchedule();
    }

    @Inject(method = "estimateStayDuration", at = @At("HEAD"), cancellable = true)
    private void railways$acceptWaypointWithoutConditions(
        int index,
        CallbackInfoReturnable<Integer> cir
    ) {
        if (schedule == null || schedule.entries.isEmpty()) {
            return;
        }

        int entryIndex = index;
        if (entryIndex >= schedule.entries.size()) {
            if (!schedule.cyclic) {
                return;
            }
            entryIndex = 0;
        }
        if (entryIndex >= 0
            && schedule.entries.get(entryIndex).instruction instanceof WaypointDestinationInstruction) {
            cir.setReturnValue(0);
        }
    }
}
