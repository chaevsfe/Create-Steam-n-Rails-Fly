/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.schedule;

import com.mojang.serialization.Codec;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.content.redstone.link.IRedstoneLinkable;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import com.zurrtum.create.content.trains.schedule.destination.ScheduleInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Emits a short wireless redstone pulse at the leading end of the train. */
public class RedstoneLinkInstruction extends ScheduleInstruction {
    private static final Codec<Couple<Frequency>> FREQUENCY_CODEC = Couple.codec(Frequency.CODEC);
    private static final WorldAttached<List<CustomRedstoneActor>> CUSTOM_ACTORS =
        new WorldAttached<>($ -> new ArrayList<>());

    public Couple<Frequency> freq;

    public RedstoneLinkInstruction(Identifier id) {
        super(id);
        freq = Couple.create(() -> Frequency.EMPTY);
        data.putInt("Power", 15);
    }

    public static void tick(Level level) {
        List<CustomRedstoneActor> actors = CUSTOM_ACTORS.get(level);
        for (Iterator<CustomRedstoneActor> iterator = actors.iterator(); iterator.hasNext(); ) {
            CustomRedstoneActor actor = iterator.next();
            actor.decrement();
            if (actor.isAlive()) {
                continue;
            }
            Create.REDSTONE_LINK_NETWORK_HANDLER.removeFromNetwork(level, actor);
            iterator.remove();
        }
    }

    @Override
    public boolean supportsConditions() {
        return false;
    }

    @Override
    protected void writeAdditional(ValueOutput view) {
        view.store("Frequency", FREQUENCY_CODEC, freq);
    }

    @Override
    protected void readAdditional(ValueInput view) {
        view.read("Frequency", FREQUENCY_CODEC).ifPresent(value -> freq = value);
    }

    @Override
    public @Nullable DiscoveredPath start(ScheduleRuntime runtime, Level level) {
        Train train = runtime.train;
        if (train.graph == null || train.carriages.isEmpty()) {
            runtime.startCooldown();
            return null;
        }

        CustomRedstoneActor actor = new CustomRedstoneActor(this, train.carriages.getFirst());
        Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(level, actor);
        CUSTOM_ACTORS.get(level).add(actor);

        runtime.state = ScheduleRuntime.State.PRE_TRANSIT;
        runtime.currentEntry++;
        return null;
    }

    private static final class CustomRedstoneActor implements IRedstoneLinkable {
        private final RedstoneLinkInstruction instruction;
        private final Carriage carriage;
        private int ticks = 8;

        private CustomRedstoneActor(RedstoneLinkInstruction instruction, Carriage carriage) {
            this.instruction = instruction;
            this.carriage = carriage;
        }

        private void decrement() {
            ticks--;
        }

        @Override
        public int getTransmittedStrength() {
            return isAlive() ? instruction.intData("Power") : 0;
        }

        @Override
        public void setReceivedStrength(int power) {
        }

        @Override
        public boolean isListening() {
            return false;
        }

        @Override
        public boolean isAlive() {
            return ticks > 0;
        }

        @Override
        public Couple<Frequency> getNetworkKey() {
            return instruction.freq;
        }

        @Override
        public BlockPos getLocation() {
            return BlockPos.containing(carriage.getLeadingPoint().getPosition(carriage.train.graph));
        }
    }
}
