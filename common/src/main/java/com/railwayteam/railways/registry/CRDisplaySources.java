package com.railwayteam.railways.registry;

import com.railwayteam.railways.content.coupling.TrackCouplerDisplaySource;
import com.railwayteam.railways.content.distant_signals.SignalDisplaySource;
import com.railwayteam.railways.content.switches.SwitchDisplaySource;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class CRDisplaySources {
    public static RegistryEntry<TrackCouplerDisplaySource> TRACK_COUPLER_INFO = RegistryEntry.empty();
    public static RegistryEntry<SwitchDisplaySource> TRACK_SWITCH = RegistryEntry.empty();
    public static RegistryEntry<SignalDisplaySource> SIGNAL = RegistryEntry.empty();

    public static void register() {
    }
}
