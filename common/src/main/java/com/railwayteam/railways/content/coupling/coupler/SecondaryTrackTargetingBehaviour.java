package com.railwayteam.railways.content.coupling.coupler;

import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.nbt.CompoundTag;

public class SecondaryTrackTargetingBehaviour<T extends TrackEdgePoint> extends TrackTargetingBehaviour<T> {
	public static final BehaviourType<SecondaryTrackTargetingBehaviour<?>> TYPE = new BehaviourType<>();

	public SecondaryTrackTargetingBehaviour(SmartBlockEntity te, EdgePointType<T> edgePointType) {
		super(te, edgePointType);
	}

	public BehaviourType<?> getType() {
		return TYPE;
	}

	public void write(CompoundTag nbt, boolean clientPacket) {
	}

	public void read(CompoundTag nbt, boolean clientPacket) {
	}
}
