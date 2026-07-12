package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.resources.Identifier;

public class RegistryEntry<T> implements NonNullSupplier<T> {
    protected final Identifier id;
    protected final T value;

    public RegistryEntry(Identifier id, T value) {
        this.id = id;
        this.value = value;
    }

    public Identifier getId() {
        return id;
    }

    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public T get() {
        return value;
    }

    public boolean is(T other) {
        return value == other;
    }

    public static <T> RegistryEntry<T> empty() {
        return new RegistryEntry<>(Identifier.fromNamespaceAndPath("railways", "empty"), null);
    }
}
