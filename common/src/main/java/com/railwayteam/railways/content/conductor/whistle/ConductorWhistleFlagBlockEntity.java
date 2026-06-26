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

package com.railwayteam.railways.content.conductor.whistle;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.schedule.Schedule;
import com.zurrtum.create.content.trains.schedule.destination.DestinationInstruction;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class ConductorWhistleFlagBlockEntity extends SmartBlockEntity implements TransformableBlockEntity {

    private static final String LOG_PREFIX = "[ConductorWhistleFlag]";

    public TrackTargetingBehaviour<GlobalStation> station;
    private boolean tickedOnce = false;
    private DyeColor color = DyeColor.RED;

    public ConductorWhistleFlagBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setLazyTickRate(100);
    }

    protected String targetStationName() {
        return ConductorWhistleItem.SPECIAL_MARKER + this.getBlockPos().toShortString();
    }

    DyeColor getColor() {
        return color;
    }
    public void lazyTick() {
        super.lazyTick();
        if (level.isClientSide())
            return;

        if (station.getEdgePoint() == null) {
            Railways.LOGGER.info("{} lazyTick: no edge point yet at {}; targetTrack={} validTrack={}",
                LOG_PREFIX, getBlockPos(), station.getGlobalPosition(), station.hasValidTrack());
            station.tick();
            Railways.LOGGER.info("{} lazyTick: edge point after tick at {} -> {}",
                LOG_PREFIX, getBlockPos(), station.getEdgePoint() == null ? "<none>" : station.getEdgePoint().getId());
        }
        if (station.getEdgePoint() != null) {
            station.getEdgePoint().name = targetStationName();
            Railways.LOGGER.info("{} lazyTick: station active at {} name={}",
                LOG_PREFIX, getBlockPos(), station.getEdgePoint().name);
        }

        if (tickedOnce) {
            boolean found = false;
            for (Train train : Create.RAILWAYS.trains.values()) {
                Schedule schedule = train.runtime == null ? null : train.runtime.getSchedule();
                if (schedule != null && schedule.entries.size() == 1 && schedule.entries.get(0).instruction instanceof DestinationInstruction destInst &&
                        destInst.getData() != null && destInst.getData().getStringOr("Text", "").equals(targetStationName())) {
                    if (!train.runtime.completed) {
                        found = true;
                        Railways.LOGGER.info("{} lazyTick: keeping flag at {}; train={} still targets {} completed={} navDestination={}",
                            LOG_PREFIX, getBlockPos(), train.id, targetStationName(), train.runtime.completed,
                            train.navigation.destination == null ? "<none>" : train.navigation.destination.name);
                        break;
                    } else {
                        Railways.LOGGER.info("{} lazyTick: discarding completed auto schedule for train={} at flag {}",
                            LOG_PREFIX, train.id, getBlockPos());
                        train.runtime.discardSchedule();
                    }
                }
            }
            if (!found) {
                Railways.LOGGER.info("{} lazyTick: removing flag at {}; no train schedule targets {}",
                    LOG_PREFIX, getBlockPos(), targetStationName());
                level.setBlock(this.getBlockPos(), Blocks.AIR.defaultBlockState(), 3);
                return;
            }
        } else {
            Railways.LOGGER.info("{} lazyTick: first tick complete at {}; stationName={}",
                LOG_PREFIX, getBlockPos(), targetStationName());
            tickedOnce = true;
        }
    }
    public void transform(BlockEntity blockEntity, StructureTransform transform) {
        station.transform(blockEntity, transform);
    }
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        station = new TrackTargetingBehaviour<>(this, EdgePointType.STATION);
        behaviours.add(station);
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.putByte("SelectedColor", ConductorEntity.idFrom(color));
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        color = ConductorEntity.colorFrom(input.getByteOr("SelectedColor", (byte) 0));
    }
}
