/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.base.registration;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class MultiRegistryCallback<A, B> {
	private static List<Runnable> finalizers = new ArrayList<>();

	private MultiRegistryCallback() {
	}

	public static <A, B> void create(
		AbstractRegistrate<?> registrateA, ResourceKey<? extends Registry<A>> typeA, Identifier idA,
		AbstractRegistrate<?> registrateB, ResourceKey<? extends Registry<B>> typeB, Identifier idB,
		BiConsumer<A, B> callback
	) {
	}

	public static void addFinalizer(Runnable finalizer) {
		if (finalizers != null)
			finalizers.add(finalizer);
	}

	public static void enableFinalizers() {
		if (finalizers == null)
			return;
		for (Runnable finalizer : finalizers)
			finalizer.run();
		finalizers = null;
	}
}
