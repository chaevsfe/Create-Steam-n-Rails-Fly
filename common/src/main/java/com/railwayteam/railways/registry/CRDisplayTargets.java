package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.distant_signals.SemaphoreDisplayTarget;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class CRDisplayTargets {
    public static RegistryEntry<SemaphoreDisplayTarget> SEMAPHORE = RegistryEntry.empty();

    private static boolean registered;

    public static void register() {
        if (registered)
            throw new IllegalStateException("Railways display targets were registered more than once");

        SEMAPHORE = register("semaphore", SemaphoreDisplayTarget::new);
        registered = true;
    }

    private static <T extends DisplayTarget> RegistryEntry<T> register(String name, Supplier<T> factory) {
        Identifier id = Railways.asResource(name);
        T value = Registry.register(CreateRegistries.DISPLAY_TARGET, id, factory.get());
        return new RegistryEntry<>(id, value);
    }
}
