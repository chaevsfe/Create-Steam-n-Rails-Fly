package com.railwayteam.railways.shim.registrate.util.nullness;

import java.util.function.Function;

@FunctionalInterface
public interface NonNullFunction<T, R> extends Function<T, R> {
}
