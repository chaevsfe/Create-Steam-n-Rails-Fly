/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.schedule;

import com.zurrtum.create.content.trains.schedule.destination.DestinationInstruction;
import net.minecraft.resources.Identifier;

/** A destination filter that routes through a station without braking there. */
public class WaypointDestinationInstruction extends DestinationInstruction {
    public WaypointDestinationInstruction(Identifier id) {
        super(id);
    }

    @Override
    public boolean supportsConditions() {
        return false;
    }
}
