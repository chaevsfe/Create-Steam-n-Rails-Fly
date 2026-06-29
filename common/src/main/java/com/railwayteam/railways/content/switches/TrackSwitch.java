/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.switches;

import com.railwayteam.railways.content.switches.TrackSwitchBlock.SwitchState;
import com.railwayteam.railways.mixin_interfaces.ISwitchDisabledEdge;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation;
import com.zurrtum.create.content.trains.signal.SingleBlockEntityEdgePoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TrackSwitch extends SingleBlockEntityEdgePoint {
	private static int selectionPriorityTicker;

	private TrackNodeLocation switchPoint;
	private final List<TrackNodeLocation> exits = new ArrayList<>();
	private @NotNull SwitchState switchState = SwitchState.NORMAL;
	private boolean automatic;
	private boolean locked;
	private boolean autoTrainsSwitch;
	private boolean forceTickClient;

	@ApiStatus.Internal
	public static int getSelectionPriority() {
		return ++selectionPriorityTicker;
	}

	public EdgePointType<?> getType() {
		return CREdgePointTypes.SWITCH;
	}

	public boolean canCoexistWith(EdgePointType<?> otherType, boolean front) {
		return otherType == EdgePointType.SIGNAL;
	}

	public void blockEntityAdded(BlockEntity tile, boolean front) {
		super.blockEntityAdded(tile, front);
		if (tile instanceof TrackSwitchBlockEntity te) {
			te.calculateExits(this);
			automatic = te.isAutomatic();
			locked = te.isLocked();
		}
	}

	public void onRemoved(TrackGraph graph) {
		exits.clear();
	}

	public boolean shouldAutoTrainsSwitch() {
		return autoTrainsSwitch;
	}

	void setAutoTrainsSwitch(boolean autoTrainsSwitch) {
		this.autoTrainsSwitch = autoTrainsSwitch;
	}

	public boolean isAutomatic() {
		return automatic;
	}

	public boolean isLocked() {
		return locked;
	}

	void setLocked(boolean locked) {
		this.locked = locked;
	}

	@ApiStatus.Internal
	public @Nullable SwitchState getTargetState(TrackNodeLocation loc) {
		if (loc == null)
			return null;
		if (loc.equals(getSwitchTarget()))
			return switchState;
		return null;
	}

	void updateExits(TrackNodeLocation switchPoint, Collection<TrackNodeLocation> newExits) {
		this.switchPoint = switchPoint;
		exits.clear();
		exits.addAll(newExits);
		ensureValidState();
	}

	@Nullable TrackNodeLocation getSwitchPoint() {
		return switchPoint;
	}

	Collection<TrackNodeLocation> getExits() {
		return List.copyOf(exits);
	}

	void ensureValidState() {
		if (!isStateValid(switchState))
			switchState = exits.isEmpty() ? SwitchState.NORMAL : SwitchState.NORMAL;
	}

	private boolean isStateValid(SwitchState state) {
		return state == SwitchState.NORMAL || !exits.isEmpty();
	}

	public boolean trySetSwitchState(@NotNull SwitchState state) {
		if (locked)
			return false;
		return setSwitchState(state);
	}

	public boolean setSwitchState(@NotNull SwitchState state) {
		if (state == null || !isStateValid(state))
			return false;
		if (switchState != state) {
			switchState = state;
			forceTickClient = true;
			return true;
		}
		return false;
	}

	public @NotNull SwitchState getSwitchState() {
		return switchState == null ? SwitchState.NORMAL : switchState;
	}

	public @Nullable TrackNodeLocation getSwitchTarget() {
		if (exits.isEmpty())
			return null;
		int index = switch (getSwitchState()) {
			case NORMAL -> 0;
			case REVERSE_LEFT -> Math.min(1, exits.size() - 1);
			case REVERSE_RIGHT -> exits.size() - 1;
		};
		return exits.get(index);
	}

	public boolean hasStraightExit() {
		return !exits.isEmpty();
	}

	public boolean hasLeftExit() {
		return exits.size() > 1;
	}

	public boolean hasRightExit() {
		return exits.size() > 1;
	}

	public void write(CompoundTag nbt, DimensionPalette dimensions) {
		nbt.putString("SwitchState", getSwitchState().getSerializedName());
		nbt.putBoolean("Automatic", automatic);
		nbt.putBoolean("Locked", locked);
		nbt.putBoolean("AutoTrainsSwitch", autoTrainsSwitch);
	}

	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.write(buffer, dimensions);
		buffer.writeInt(getSwitchState().ordinal());
		buffer.writeBoolean(automatic);
		buffer.writeBoolean(locked);
		buffer.writeBoolean(autoTrainsSwitch);
		boolean hasPoint = switchPoint != null;
		buffer.writeBoolean(hasPoint);
		if (hasPoint) {
			switchPoint.send(buffer, dimensions);
			buffer.writeCollection(exits, (buf, loc) -> loc.send(buf, dimensions));
		}
	}

	public void read(CompoundTag nbt, boolean migration, DimensionPalette dimensions) {
		String exit = nbt.getString("SwitchState").orElse(SwitchState.NORMAL.getSerializedName());
		try {
			switchState = SwitchState.valueOf(exit.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			switchState = SwitchState.NORMAL;
		}
		automatic = nbt.getBoolean("Automatic").orElse(false);
		locked = nbt.getBoolean("Locked").orElse(false);
		autoTrainsSwitch = nbt.getBoolean("AutoTrainsSwitch").orElse(false);
	}

	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.read(buffer, dimensions);
		switchState = SwitchState.values()[buffer.readInt()];
		automatic = buffer.readBoolean();
		locked = buffer.readBoolean();
		autoTrainsSwitch = buffer.readBoolean();
		if (buffer.readBoolean()) {
			updateExits(
				TrackNodeLocation.receive(buffer, dimensions),
				buffer.readList(buf -> TrackNodeLocation.receive(buf, dimensions))
			);
		}
	}

	boolean doForceTickClient() {
		if (!forceTickClient)
			return false;
		forceTickClient = false;
		return true;
	}

	public void tick(TrackGraph graph, boolean preTrains) {
		if (preTrains) {
			updateEdges(graph);
			if (automatic)
				switchForEdges(graph);
		}
	}

	public void updateEdges(TrackGraph graph) {
		updateEdges(graph, false);
	}

	public void setEdgesActive(TrackGraph graph) {
		updateEdges(graph, true);
	}

	private void updateEdges(TrackGraph graph, boolean forceActive) {
		TrackNodeLocation from = switchPoint;
		for (TrackNodeLocation to : exits) {
			if (to == null)
				continue;

			TrackNode toNode = graph.locateNode(to);
			Map<TrackNode, TrackEdge> connections = graph.getConnectionsFrom(toNode);
			if (connections == null)
				continue;

			TrackNode closestFromNode = null;
			TrackEdge closestEdge = null;
			double closestDistance = Double.MAX_VALUE;
			for (Map.Entry<TrackNode, TrackEdge> otherEnd : connections.entrySet()) {
				double distance = otherEnd.getKey().getLocation().distSqr(from);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestEdge = otherEnd.getValue();
					closestFromNode = otherEnd.getKey();
				}
			}

			boolean enabled = forceActive || getSwitchTarget() == to;
			boolean autoSelectable = !forceActive && automatic && !locked && autoTrainsSwitch;
			if (closestEdge != null)
				updateSwitchEdge(closestEdge, enabled, autoSelectable);

			if (closestFromNode != null) {
				TrackEdge reverseEdge = graph.getConnection(Couple.create(closestFromNode, toNode));
				if (reverseEdge != null)
					updateSwitchEdge(reverseEdge, enabled, autoSelectable);
			}
		}
	}

	private static void updateSwitchEdge(TrackEdge edge, boolean enabled, boolean autoSelectable) {
		ISwitchDisabledEdge switchEdge = (ISwitchDisabledEdge) edge.getEdgeData();
		switchEdge.setEnabled(enabled);
		switchEdge.setAutomatic(autoSelectable);
	}

	private void switchForEdges(TrackGraph graph) {
		TrackNodeLocation from = switchPoint;
		TrackNodeLocation highestPriorityExit = null;
		int highestPriority = -100;
		for (TrackNodeLocation to : exits) {
			if (to == null)
				continue;

			TrackNode toNode = graph.locateNode(to);
			Map<TrackNode, TrackEdge> connections = graph.getConnectionsFrom(toNode);
			if (connections == null)
				continue;

			TrackNode closestFromNode = null;
			TrackEdge closestEdge = null;
			double closestDistance = Double.MAX_VALUE;
			for (Map.Entry<TrackNode, TrackEdge> otherEnd : connections.entrySet()) {
				double distance = otherEnd.getKey().getLocation().distSqr(from);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestEdge = otherEnd.getValue();
					closestFromNode = otherEnd.getKey();
				}
			}

			if (closestEdge != null) {
				ISwitchDisabledEdge switchEdge = (ISwitchDisabledEdge) closestEdge.getEdgeData();
				if (switchEdge.isAutomaticallySelected()) {
					if (switchEdge.getAutomaticallySelectedPriority() > highestPriority) {
						highestPriorityExit = to;
						highestPriority = switchEdge.getAutomaticallySelectedPriority();
					}
					switchEdge.ackAutomaticSelection();
				}
			}

			if (closestFromNode != null) {
				TrackEdge reverseEdge = graph.getConnection(Couple.create(closestFromNode, toNode));
				if (reverseEdge != null) {
					ISwitchDisabledEdge reverseSwitchEdge = (ISwitchDisabledEdge) reverseEdge.getEdgeData();
					if (reverseSwitchEdge.isAutomaticallySelected()) {
						if (reverseSwitchEdge.getAutomaticallySelectedPriority() > highestPriority) {
							highestPriorityExit = to;
							highestPriority = reverseSwitchEdge.getAutomaticallySelectedPriority();
						}
						reverseSwitchEdge.ackAutomaticSelection();
					}
				}
			}
		}

		if (highestPriorityExit != null) {
			for (SwitchState state : SwitchState.values()) {
				if (highestPriorityExit == getSwitchTarget(state)) {
					setSwitchState(state);
					break;
				}
			}
		}
	}

	private @Nullable TrackNodeLocation getSwitchTarget(SwitchState state) {
		if (exits.isEmpty())
			return null;
		int index = switch (state) {
			case NORMAL -> 0;
			case REVERSE_LEFT -> Math.min(1, exits.size() - 1);
			case REVERSE_RIGHT -> exits.size() - 1;
		};
		return exits.get(index);
	}
}
