/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.coupling.coupler;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.signal.SingleBlockEntityEdgePoint;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class TrackCoupler extends SingleBlockEntityEdgePoint {
	private int activated;
	private UUID currentTrain;

	public EdgePointType<?> getType() {
		return CREdgePointTypes.COUPLER;
	}

	@Override
	public void tick(MinecraftServer server, TrackGraph graph, boolean preTrains) {
		super.tick(server, graph, preTrains);
		if (activated > 0)
			activated--;
		if (activated <= 0)
			currentTrain = null;
	}

	public UUID getCurrentTrain() {
		return currentTrain;
	}

	public boolean isActivated() {
		return activated > 0;
	}

	public void keepAlive(Train train) {
		if (((IHandcarTrain) train).railways$isHandcar())
			return;
		activated = 8;
		currentTrain = train.id;
	}

	public void blockEntityAdded(BlockEntity tile, boolean front) {
		super.blockEntityAdded(tile, front);
	}

	@Override
	public void read(ValueInput input, boolean migration, DimensionPalette dimensions) {
		super.read(input, migration, dimensions);
		activated = input.getIntOr("Activated", 0);
		currentTrain = input.getString("TrainId").map(UUID::fromString).orElse(null);
	}

	@Override
	public <T> void decode(DynamicOps<T> ops, T input, boolean migration, DimensionPalette dimensions) {
		super.decode(ops, input, migration, dimensions);
		if (migration)
			return;
		MapLike<T> map = ops.getMap(input).getOrThrow();
		activated = Optional.ofNullable(map.get("Activated")).map(value -> ops.getNumberValue(value, 0).intValue()).orElse(0);
		currentTrain = Optional.ofNullable(map.get("TrainId"))
			.flatMap(value -> ops.getStringValue(value).result())
			.map(UUID::fromString)
			.orElse(null);
	}

	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.read(buffer, dimensions);
		if (buffer.readBoolean())
			blockEntityPos = buffer.readBlockPos();
	}

	@Override
	public void write(ValueOutput output, DimensionPalette dimensions) {
		super.write(output, dimensions);
		output.putInt("Activated", activated);
		if (currentTrain != null)
			output.putString("TrainId", currentTrain.toString());
	}

	@Override
	public <T> DataResult<T> encode(DynamicOps<T> ops, T prefix, DimensionPalette dimensions) {
		DataResult<T> result = super.encode(ops, prefix, dimensions);
		RecordBuilder<T> builder = ops.mapBuilder();
		builder.add("Activated", ops.createInt(activated));
		if (currentTrain != null)
			builder.add("TrainId", ops.createString(currentTrain.toString()));
		return builder.build(result);
	}

	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.write(buffer, dimensions);
		buffer.writeBoolean(blockEntityPos != null);
		if (blockEntityPos != null)
			buffer.writeBlockPos(blockEntityPos);
	}
}
