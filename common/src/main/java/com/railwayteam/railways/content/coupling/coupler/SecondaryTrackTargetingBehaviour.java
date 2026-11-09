/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.coupling.coupler;

import com.railwayteam.railways.mixin.AccessorTrackTargetingBehavior;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class SecondaryTrackTargetingBehaviour<T extends TrackEdgePoint> extends TrackTargetingBehaviour<T> {
	public static final BehaviourType<SecondaryTrackTargetingBehaviour<?>> TYPE = new BehaviourType<>();

	public SecondaryTrackTargetingBehaviour(SmartBlockEntity te, EdgePointType<T> edgePointType) {
		super(te, edgePointType);
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}

	@Override
	public void write(ValueOutput output, boolean clientPacket) {
		AccessorTrackTargetingBehavior accessor = (AccessorTrackTargetingBehavior) this;
		output.store("SecondaryId", UUIDUtil.CODEC, accessor.getId());
		output.store("SecondaryTargetTrack", BlockPos.CODEC, accessor.getTargetTrack());
		output.putBoolean("SecondaryOrtho", accessor.isOrthogonal());
		output.putBoolean("SecondaryTargetDirection", accessor.getTargetDirection() == Direction.AxisDirection.POSITIVE);
		if (accessor.getRotatedDirection() != null)
			output.store("SecondaryRotatedAxis", Vec3.CODEC, accessor.getRotatedDirection());
		if (accessor.getPrevDirection() != null)
			output.store("SecondaryPrevAxis", Vec3.CODEC, accessor.getPrevDirection());
		if (accessor.getMigrationData() != null && !clientPacket)
			output.store("SecondaryMigrate", CompoundTag.CODEC, accessor.getMigrationData());
		if (accessor.getTargetBezier() != null) {
			ValueOutput bezier = output.child("SecondaryBezier");
			bezier.putInt("Segment", accessor.getTargetBezier().segment());
			bezier.store("Key", BlockPos.CODEC, accessor.getTargetBezier().curveTarget()
				.subtract(getPos()));
		}
	}

	@Override
	public void read(ValueInput input, boolean clientPacket) {
		AccessorTrackTargetingBehavior accessor = (AccessorTrackTargetingBehavior) this;
		accessor.setId(input.read("SecondaryId", UUIDUtil.CODEC).orElseGet(UUID::randomUUID));
		accessor.setTargetTrack(input.read("SecondaryTargetTrack", BlockPos.CODEC).orElse(BlockPos.ZERO));
		accessor.setTargetDirection(input.getBooleanOr("SecondaryTargetDirection", false)
			? Direction.AxisDirection.POSITIVE
			: Direction.AxisDirection.NEGATIVE);
		accessor.setOrthogonal(input.getBooleanOr("SecondaryOrtho", false));
		input.read("SecondaryPrevAxis", Vec3.CODEC).ifPresent(accessor::setPrevDirection);
		input.read("SecondaryRotatedAxis", Vec3.CODEC).ifPresent(accessor::setRotatedDirection);
		input.read("SecondaryMigrate", CompoundTag.CODEC).ifPresent(accessor::setMigrationData);
		if (clientPacket)
			accessor.setEdgePoint(null);
		input.child("SecondaryBezier").ifPresent(bezier -> {
			BlockPos key = bezier.read("Key", BlockPos.CODEC).orElse(BlockPos.ZERO);
			accessor.setTargetBezier(new BezierTrackPointLocation(key.offset(getPos()), bezier.getIntOr("Segment", 0)));
		});
	}
}
