/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.content.extended_sliding_doors;

import net.minecraft.nbt.CompoundTag;

import java.util.Locale;

public enum SlidingDoorMode {
    NORMAL, //shouldOpen -> noChange; shouldUpdate -> noChange; - default behaviour
    MANUAL { //shouldOpen -> noChange; shouldUpdate -> never; // done block redstone operation
        public boolean canOpenSpecially() {
            return false;
        }
    },
    SPECIAL { //shouldOpen -> &= at right station; shouldUpdate -> noChange; // done block hand operation (shift-use works on trains/contraptions though)
        public boolean canOpenManually() {
            return false;
        }
    },
    SPECIAL_INVERTED { //shouldOpen -> &= at right station; shouldUpdate -> !shouldUpdate; // done block hand operation (shift-use works on trains/contraptions though)
        public boolean canOpenManually() {
            return false;
        }
    },
    ;

    public boolean canOpenManually() {
        return true;
    }
    public boolean canOpenSpecially() {
        return true;
    }
    public String getTranslationKey() {
        return "sliding_door.mode." + name().toLowerCase(Locale.ROOT);
    }

    public static SlidingDoorMode fromNbt(CompoundTag nbt) {
        if (nbt == null)
            return SlidingDoorMode.NORMAL;
        SlidingDoorMode[] modes = SlidingDoorMode.values();
        int modeIndex = Math.min(modes.length - 1, Math.max(0, nbt.getInt("ScrollValue").orElse(0)));
        return modes[modeIndex];
    }

    public interface IHasDoorMode {
        SlidingDoorMode railways$getSlidingDoorMode();
    }
}
