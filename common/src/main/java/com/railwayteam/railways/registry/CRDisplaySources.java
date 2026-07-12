package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.coupling.TrackCouplerDisplaySource;
import com.railwayteam.railways.content.distant_signals.SignalDisplaySource;
import com.railwayteam.railways.content.switches.SwitchDisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.tterrag.registrate.util.entry.RegistryEntry;
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
