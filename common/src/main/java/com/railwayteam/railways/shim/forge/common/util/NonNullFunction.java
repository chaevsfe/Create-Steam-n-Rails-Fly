package com.railwayteam.railways.shim.forge.common.util;

import java.util.function.Function;

@FunctionalInterface
public interface NonNullFunction<T, R> extends Function<T, R> {
}
