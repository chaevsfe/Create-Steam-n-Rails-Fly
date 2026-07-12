/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.schedule.client.RedstoneLinkInstructionRender;
import com.railwayteam.railways.content.schedule.client.StationLoadedConditionRender;
import com.zurrtum.create.client.AllScheduleRenders;
import com.zurrtum.create.client.content.trains.schedule.destination.DestinationInstructionRender;

/** Attaches Create Fly's client-only editors to Railways schedule entries. */
public final class CRScheduleClient {
    private static boolean registered;

    private CRScheduleClient() {
    }

    public static synchronized void register() {
        if (!registered) {
            AllScheduleRenders.ALL.put(CRSchedule.REDSTONE_LINK, new RedstoneLinkInstructionRender());
            AllScheduleRenders.ALL.put(CRSchedule.WAYPOINT_DESTINATION, new DestinationInstructionRender());
            AllScheduleRenders.ALL.put(CRSchedule.LOADED, new StationLoadedConditionRender());
            registered = true;
        }

        if (!(AllScheduleRenders.ALL.get(CRSchedule.REDSTONE_LINK) instanceof RedstoneLinkInstructionRender)
            || !(AllScheduleRenders.ALL.get(CRSchedule.WAYPOINT_DESTINATION)
                instanceof DestinationInstructionRender)
            || !(AllScheduleRenders.ALL.get(CRSchedule.LOADED) instanceof StationLoadedConditionRender)) {
            throw new IllegalStateException("Railways custom schedule client renders are incomplete");
        }

        Railways.LOGGER.info("Railways custom schedule client renders verified");
    }
}
