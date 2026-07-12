/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.compat.Mods;
import com.railwayteam.railways.content.animated_flywheel.FlywheelMovementBehaviour;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import net.minecraft.world.level.block.Block;

/** Registers the animated carriage flywheel actor after Create's own actor registry is ready. */
public final class CRAnimatedFlywheels {
    private CRAnimatedFlywheels() {
    }

    public static void register() {
        FlywheelMovementBehaviour.verifyMath();

        int registered = registerIfAvailable(AllBlocks.FLYWHEEL);
        for (PalettesColor color : PalettesColor.values())
            registered += registerIfAvailable(CRPalettes.Styles.FLYWHEEL.get(color).get());

        int expected = 1 + PalettesColor.values().length;
        if (!Mods.EXTENDEDFLYWHEELS.isLoaded && registered != expected)
            throw new IllegalStateException(
                "Animated flywheel behaviour was attached to " + registered + " of " + expected + " blocks"
            );

        Railways.LOGGER.info(
            "Animated flywheel movement registered for {} blocks{}",
            registered,
            Mods.EXTENDEDFLYWHEELS.isLoaded ? " (preserving Extended Flywheels registrations)" : ""
        );
    }

    private static int registerIfAvailable(Block block) {
        MovementBehaviour existing = MovementBehaviour.REGISTRY.get(block);
        if (existing == FlywheelMovementBehaviour.INSTANCE)
            return 1;
        if (existing != null) {
            if (Mods.EXTENDEDFLYWHEELS.isLoaded)
                return 0;
            throw new IllegalStateException("Unexpected movement behaviour already registered for " + block);
        }

        MovementBehaviour.REGISTRY.register(block, FlywheelMovementBehaviour.INSTANCE);
        if (MovementBehaviour.REGISTRY.get(block) != FlywheelMovementBehaviour.INSTANCE)
            throw new IllegalStateException("Animated flywheel movement registration was not retained for " + block);
        return 1;
    }
}
