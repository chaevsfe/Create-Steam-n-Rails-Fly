package net.minecraft;

import java.util.function.Consumer;

public final class Util {
    private Util() {
    }

    public static <T> T make(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

    public static Consumer<String> prefix(String prefix, Consumer<String> consumer) {
        return message -> consumer.accept(prefix + message);
    }
}
