/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.coupling.TrackCouplerDisplaySource;
import com.railwayteam.railways.content.distant_signals.SignalDisplaySource;
import com.railwayteam.railways.content.switches.SwitchDisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.railwayteam.railways.shim.registrate.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class CRDisplaySources {
    public static RegistryEntry<TrackCouplerDisplaySource> TRACK_COUPLER_INFO = RegistryEntry.empty();
    public static RegistryEntry<SwitchDisplaySource> TRACK_SWITCH = RegistryEntry.empty();
    public static RegistryEntry<SignalDisplaySource> SIGNAL = RegistryEntry.empty();

    private static boolean registered;

    public static void register() {
        if (registered)
            throw new IllegalStateException("Railways display sources were registered more than once");

        TRACK_COUPLER_INFO = register("track_coupler_info", TrackCouplerDisplaySource::new);
        TRACK_SWITCH = register("track_switch", SwitchDisplaySource::new);
        SIGNAL = register("track_signal_source", SignalDisplaySource::new);
        registered = true;
    }

    private static <T extends DisplaySource> RegistryEntry<T> register(String name, Supplier<T> factory) {
        Identifier id = Railways.asResource(name);
        T value = Registry.register(CreateRegistries.DISPLAY_SOURCE, id, factory.get());
        return new RegistryEntry<>(id, value);
    }
}
