/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 * Copyright (c) 2026 chaevsfe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.railwayteam.railways.util.MixinVariables;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.graph.EdgeData;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.signal.SignalPropagator;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = SignalPropagator.class, remap = false)
public class MixinSignalPropagator {
	@WrapMethod(
		method = "walkSignals(Lnet/minecraft/server/MinecraftServer;Lcom/zurrtum/create/content/trains/graph/TrackGraph;Ljava/util/List;Ljava/util/function/Predicate;Ljava/util/function/Predicate;Z)V"
	)
	private static void railways$trackSignalPropagatorDepth(
		MinecraftServer server,
		TrackGraph graph,
		List<Couple<TrackNode>> frontier,
		Predicate<Pair<TrackNode, SignalBoundary>> boundaryCallback,
		Predicate<EdgeData> nonBoundaryCallback,
		boolean forCollection,
		Operation<Void> original
	) {
		MixinVariables.signalPropagatorCallDepth++;
		try {
			original.call(server, graph, frontier, boundaryCallback, nonBoundaryCallback, forCollection);
		} finally {
			MixinVariables.signalPropagatorCallDepth--;
		}
	}
}
