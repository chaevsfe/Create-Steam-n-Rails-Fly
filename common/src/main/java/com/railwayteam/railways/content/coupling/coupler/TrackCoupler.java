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

import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.signal.SingleBlockEntityEdgePoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.UUID;

public class TrackCoupler extends SingleBlockEntityEdgePoint {
	private int activated;
	private UUID currentTrain;

	public EdgePointType<?> getType() {
		return CREdgePointTypes.COUPLER;
	}

	public void tick(TrackGraph graph, boolean preTrains) {
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

	public void read(CompoundTag nbt, boolean migration, DimensionPalette dimensions) {
		activated = nbt.getInt("Activated").orElse(0);
		currentTrain = nbt.getString("TrainId").map(UUID::fromString).orElse(null);
	}

	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.read(buffer, dimensions);
		if (buffer.readBoolean())
			blockEntityPos = buffer.readBlockPos();
	}

	public void write(CompoundTag nbt, DimensionPalette dimensions) {
		nbt.putInt("Activated", activated);
		if (currentTrain != null)
			nbt.putString("TrainId", currentTrain.toString());
	}

	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.write(buffer, dimensions);
		buffer.writeBoolean(blockEntityPos != null);
		if (blockEntityPos != null)
			buffer.writeBlockPos(blockEntityPos);
	}
}
