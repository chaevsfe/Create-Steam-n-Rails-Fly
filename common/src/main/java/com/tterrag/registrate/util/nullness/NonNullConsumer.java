package com.tterrag.registrate.util.nullness;

@FunctionalInterface
public interface NonNullConsumer<T> extends java.util.function.Consumer<T> {
    static <T> NonNullConsumer<T> noop() {
        return ignored -> {};
    }
}
