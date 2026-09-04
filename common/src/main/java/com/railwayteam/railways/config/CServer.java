/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

package com.railwayteam.railways.config;

import com.google.gson.JsonObject;
import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.config.ui.ConfigAnnotations;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class CServer extends ConfigBase {

    public final ConfigGroup misc = group(0, "misc", Comments.misc);

    public final ConfigBool strictCoupler = b(false, "strictCoupler", Comments.strictCoupler);
    public final ConfigBool flipDistantSwitches = b(true, "flipDistantSwitches", Comments.flipDistantSwitches);
    public final ConfigInt switchPlacementRange = i(64, 16, 128, "switchPlacementRange", Comments.switchPlacementRange);
    public final ConfigBool explosiveTrackDamage = b(false, "creeperTrackDamage", Comments.explosiveTrackDamage);
    public final ConfigFloat handcarHungerMultiplier = f(.01f, 0, 1, "handcarHungerMultiplier", Comments.handcarHungerMultiplier);
    public final ConfigBool rollersClearSnow = b(true, "rollersClearSnow", Comments.rollersClearSnow);
    public final ConfigBool unlimitedCreativeRelocation = b(false, "unlimitedCreativeRelocation", Comments.unlimitedCreativeRelocation, ConfigAnnotations.RequiresRelog.TRUE.asComment());

    public final CSemaphores semaphores = nested(0, CSemaphores::new, Comments.semaphores);
    public final CConductors conductors = nested(0, CConductors::new, Comments.conductors);
    public final CRealism realism = nested(0, CRealism::new, Comments.realism);

    private Builder builder;
    private JsonObject localValues;

    public String getName() {
        return "server";
    }

    @Override
    public void registerAll(Builder builder) {
        this.builder = builder;
        super.registerAll(builder);
    }

    public JsonObject getValues() {
        return builder == null ? null : builder.object;
    }

    public void reload(@Nullable JsonObject synced) {
        if (builder == null)
            return;

        if (synced == null) {
            if (localValues == null)
                return;
            bind(localValues);
            localValues = null;
        } else {
            if (localValues == null)
                localValues = builder.object;
            try {
                bind(synced);
            } catch (RuntimeException e) {
                bind(localValues);
                throw e;
            }
            builder.object = localValues;
        }
    }

    private void bind(JsonObject values) {
        JsonObject previous = builder.object;
        builder.object = values;
        depth = 0;
        try {
            super.registerAll(builder);
            builder.pop(depth);
        } catch (RuntimeException e) {
            unwind();
            builder.object = previous;
            throw e;
        } finally {
            depth = 0;
        }
    }

    private void unwind() {
        while (true) {
            try {
                builder.pop();
            } catch (IllegalArgumentException e) {
                return;
            }
        }
    }

    private static class Comments {
        static String misc = "Miscellaneous settings";

        static String strictCoupler = "Coupler will require points to be on the same or adjacent track edge, this will prevent the coupler from working if there is any form of junction in between the two points.";
        static String flipDistantSwitches = "Allow controlling Brass Switches remotely when approaching them on a train";
        static String switchPlacementRange = "Max distance between targeted track and placed switch stand";
        static String explosiveTrackDamage = "Allow creepers and ghast fireballs to damage tracks";
        static String handcarHungerMultiplier = "Multiplier used for calculating exhaustion from speed when a handcar is used.";
        static String rollersClearSnow = "Rollers clear snow-encased tracks when rolling over them.";
        static String unlimitedCreativeRelocation = "Allow creative mode players to relocate trains without distance restrictions.";

        static String semaphores = "Semaphore settings";
        static String conductors = "Conductor settings";
        static String realism = "Realism Settings";
    }
}
