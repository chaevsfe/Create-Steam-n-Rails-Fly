package com.tterrag.registrate.builders;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public abstract class AbstractBuilder<T, P, B extends AbstractBuilder<T, P, B>> {
    protected final Registrate owner;
    protected final String name;
    protected final P parent;
    private final List<Consumer<? super T>> registerCallbacks = new ArrayList<>();
    private final Map<ResourceKey<? extends Registry<?>>, List<Consumer<? super T>>> afterRegisterCallbacks = new HashMap<>();

    protected AbstractBuilder(Registrate owner, String name, P parent) {
        this.owner = owner;
        this.name = name;
        this.parent = parent;
    }

    public B transform(NonNullUnaryOperator<B> operator) {
        return operator.apply((B) this);
    }

    public B onRegister(NonNullConsumer<? super T> consumer) {
        registerCallbacks.add(consumer);
        return (B) this;
    }

    public <R> B onRegisterAfter(Registry<R> registry, Consumer<? super T> consumer) {
        return (B) this;
    }

    public <R> B onRegisterAfter(ResourceKey<? extends Registry<R>> registry, Consumer<? super T> consumer) {
        afterRegisterCallbacks.computeIfAbsent((ResourceKey<? extends Registry<?>>) registry, ignored -> new ArrayList<>())
            .add(consumer);
        return (B) this;
    }

    protected void runRegisterCallbacks(T value) {
        registerCallbacks.forEach(callback -> callback.accept(value));
    }

    protected void runAfterRegisterCallbacks(ResourceKey<? extends Registry<?>> registry, T value) {
        afterRegisterCallbacks.getOrDefault(registry, List.of())
            .forEach(callback -> callback.accept(value));
    }

    public B lang(String lang) {
        return (B) this;
    }

    public B defaultLang() {
        return (B) this;
    }

    public B tag(TagKey<?>... tags) {
        return (B) this;
    }

    public B recipe(BiConsumer<?, ?> consumer) {
        return (B) this;
    }

    public B model(BiConsumer<?, ?> consumer) {
        return (B) this;
    }

    public P build() {
        return parent;
    }
}
