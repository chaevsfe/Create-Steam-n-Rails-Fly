package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceLocation;

public class RegistryEntry<T> implements NonNullSupplier<T> {
    protected final Identifier identifier;
    protected final ResourceLocation id;
    protected final T value;

    public RegistryEntry(Identifier id, T value) {
        this.identifier = id;
        this.id = new ResourceLocation(id.getNamespace(), id.getPath());
        this.value = value;
    }

    public ResourceLocation getId() {
        return id;
    }

    public Identifier getIdentifier() {
        return identifier;
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
