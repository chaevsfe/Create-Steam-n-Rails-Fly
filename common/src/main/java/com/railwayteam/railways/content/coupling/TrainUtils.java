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

package com.railwayteam.railways.content.coupling;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.mixin.AccessorTrain;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.railwayteam.railways.multiloader.PlayerSelection;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.util.packet.AddTrainEndPacket;
import com.railwayteam.railways.util.packet.CarriageContraptionEntityUpdatePacket;
import com.railwayteam.railways.util.packet.ChopTrainEndPacket;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Navigation;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.infrastructure.packet.s2c.AddTrainPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TrainUtils {
    private static final int FIRST = 0, BOTH = 3;

    public static Train splitTrain(Train train, int numberOffEnd) {
        if (((IHandcarTrain) train).railways$isHandcar())
            return train;
        if (numberOffEnd == 0)
            return train;
        if (train.carriages.size() <= numberOffEnd)
            return train;
        if (!allCarriagesLoaded(train))
            return train;

        Integer frontSpacingBackup = null;
        Carriage[] lastCarriages = new Carriage[numberOffEnd];
        Integer[] lastCarriageSpacings = new Integer[numberOffEnd - 1];

        for (int i = numberOffEnd - 1; i >= 0; i--) {
            lastCarriages[i] = train.carriages.remove(train.carriages.size() - 1);
            if (i > 0) {
                lastCarriageSpacings[i - 1] = train.carriageSpacing.remove(train.carriageSpacing.size() - 1);
            } else {
                frontSpacingBackup = train.carriageSpacing.remove(train.carriageSpacing.size() - 1);
            }
        }

        double[] originalStress = ((AccessorTrain) train).railways$getStress();
        double[] newStress = new double[Math.max(0, originalStress.length - numberOffEnd)];
        System.arraycopy(originalStress, 0, newStress, 0, newStress.length);
        ((AccessorTrain) train).railways$setStress(newStress);

        Train newTrain;
        try {
            newTrain = new Train(UUID.randomUUID(), train.owner, train.graph, new ArrayList<>(List.of(lastCarriages)),
                new ArrayList<>(List.of(lastCarriageSpacings)), hasBackwardControls(lastCarriages), 0);
        } catch (RuntimeException e) {
            train.carriages.addAll(List.of(lastCarriages));
            if (frontSpacingBackup != null)
                train.carriageSpacing.add(frontSpacingBackup);
            train.carriageSpacing.addAll(List.of(lastCarriageSpacings));
            ((AccessorTrain) train).railways$setStress(originalStress);
            Railways.LOGGER.warn("Failed to split train {}", train.id, e);
            return train;
        }

        train.doubleEnded = hasBackwardControls(train.carriages);
        if (!train.name.getString().contains("Split off from: "))
            newTrain.name = Component.literal("Split off from: " + train.name.getString());
        else
            newTrain.name = Component.literal(train.name.getString());

        for (int i = 0; i < lastCarriages.length; i++) {
            Carriage carriage = lastCarriages[i];
            int carriageIndex = i;
            carriage.setTrain(newTrain);
            carriage.forEachPresentEntity(cce -> {
                cce.carriageIndex = carriageIndex;
                cce.trainId = newTrain.id;
                cce.setCarriage(carriage);
                cce.syncCarriage();
            });
        }

        for (int i = 0; i < train.carriages.size(); i++) {
            Carriage carriage = train.carriages.get(i);
            int carriageIndex = i;
            carriage.setTrain(train);
            carriage.forEachPresentEntity(cce -> {
                cce.carriageIndex = carriageIndex;
                cce.trainId = train.id;
                cce.setCarriage(carriage);
                cce.syncCarriage();
            });
        }

        train.updateSignalBlocks = true;
        train.collectInitiallyOccupiedSignalBlocks();
        newTrain.collectInitiallyOccupiedSignalBlocks();

        Create.RAILWAYS.addTrain(newTrain);
        PlayerSelection allPlayers = PlayerSelection.all();
        CRPackets.PACKETS.sendTo(allPlayers, new AddTrainPacket(newTrain));
        CRPackets.PACKETS.sendTo(allPlayers, new ChopTrainEndPacket(train, numberOffEnd, train.doubleEnded));
        Arrays.stream(lastCarriages).forEach(carriage -> carriage.forEachPresentEntity(
            cce -> CRPackets.PACKETS.sendTo(allPlayers, new CarriageContraptionEntityUpdatePacket(cce, newTrain))));
        train.carriages.forEach(carriage -> carriage.forEachPresentEntity(
            cce -> CRPackets.PACKETS.sendTo(allPlayers, new CarriageContraptionEntityUpdatePacket(cce, train))));

        if (train.carriages.isEmpty())
            Create.RAILWAYS.removeTrain(train.id);

        tryToParkNearby(newTrain, 0.75);
        newTrain.collectInitiallyOccupiedSignalBlocks();

        return newTrain;
    }

    public static void tryToParkNearby(Train train, double maxDistance) {
        if (train.carriages.isEmpty() || train.graph == null)
            return;
        final double offsetDist = 0.05;
        Carriage leadingCarriage = train.carriages.get(0);
        int leadingType = train.carriages.size() == 1 ? BOTH : FIRST;
        leadingCarriage.travel(null, train.graph, -offsetDist, null, null, leadingType);
        TravellingPoint discoveryPoint = copy(leadingCarriage.getLeadingPoint());
        MutableObject<GlobalStation> targetStation = new MutableObject<>(null);
        double distance = discoveryPoint.travel(train.graph, maxDistance + offsetDist,
            discoveryPoint.steer(TravellingPoint.SteerDirection.NONE, new Vec3(0, 1, 0)),
            (Double a, Pair<TrackEdgePoint, Couple<TrackNode>> couple) -> {
                if (couple.getFirst() instanceof GlobalStation station && station.canApproachFrom(couple.getSecond().getSecond())
                    && (station.getNearestTrain() == null || station.getNearestTrain() == train) && station.getPresentTrain() == null) {
                    targetStation.setValue(station);
                    return true;
                }
                return false;
            });

        if (targetStation.getValue() != null) {
            Navigation oldNavigation = train.navigation;
            ScheduleRuntime oldRuntime = train.runtime;
            train.navigation = new Navigation(train);
            train.runtime = new ScheduleRuntime(train);
            train.navigation.destination = targetStation.getValue();
            leadingCarriage.travel(null, train.graph, Math.max(0.01, distance) + offsetDist, discoveryPoint, null, leadingType);
            targetStation.getValue().reserveFor(train);
            train.navigation.train = null;
            train.runtime = oldRuntime;
            train.navigation = oldNavigation;
        } else {
            leadingCarriage.travel(null, train.graph, offsetDist, null, null, leadingType);
        }
    }

    private static TravellingPoint copy(TravellingPoint original) {
        TravellingPoint copy = new TravellingPoint(original.node1, original.node2, original.edge, original.position, original.upsideDown);
        copy.blocked = original.blocked;
        return copy;
    }

    public static Train combineTrains(Train frontTrain, Train backTrain, BlockPos itemDropPos, Level itemDropLevel, int carriageSpacing) {
        return combineTrains(frontTrain, backTrain, Vec3.atBottomCenterOf(itemDropPos), itemDropLevel, carriageSpacing);
    }

    /**
     * Adds the carriages of backTrain onto the end of frontTrain.
     */
    public static Train combineTrains(Train frontTrain, Train backTrain, Vec3 itemDropPos, Level itemDropLevel, int carriageSpacing) {
        if (((IHandcarTrain) frontTrain).railways$isHandcar() || ((IHandcarTrain) backTrain).railways$isHandcar())
            return frontTrain;
        if (frontTrain.derailed || backTrain.derailed)
            return frontTrain;
        if (!allCarriagesLoaded(frontTrain) || !allCarriagesLoaded(backTrain))
            return frontTrain;

        frontTrain.carriages.addAll(backTrain.carriages);
        backTrain.carriages.clear();

        frontTrain.carriageSpacing.add(carriageSpacing);
        frontTrain.carriageSpacing.addAll(backTrain.carriageSpacing);
        backTrain.carriageSpacing.clear();

        double[] frontStress = ((AccessorTrain) frontTrain).railways$getStress();
        double[] backStress = ((AccessorTrain) backTrain).railways$getStress();
        double[] newStress = new double[frontStress.length + backStress.length + 1];
        System.arraycopy(frontStress, 0, newStress, 0, frontStress.length);
        System.arraycopy(backStress, 0, newStress, frontStress.length + 1, backStress.length);
        ((AccessorTrain) frontTrain).railways$setStress(newStress);

        frontTrain.doubleEnded = hasBackwardControls(frontTrain.carriages);
        for (int i = 0; i < frontTrain.carriages.size(); i++) {
            Carriage carriage = frontTrain.carriages.get(i);
            int carriageIndex = i;
            carriage.setTrain(frontTrain);
            carriage.forEachPresentEntity(cce -> {
                cce.carriageIndex = carriageIndex;
                cce.trainId = frontTrain.id;
                cce.setCarriage(carriage);
            });
        }

        if (backTrain.getCurrentStation() != null)
            backTrain.getCurrentStation().cancelReservation(backTrain);

        frontTrain.updateSignalBlocks = true;
        frontTrain.collectInitiallyOccupiedSignalBlocks();
        Create.RAILWAYS.removeTrain(backTrain.id);

        PlayerSelection allPlayers = PlayerSelection.all();
        CRPackets.PACKETS.sendTo(allPlayers, new AddTrainEndPacket(frontTrain, backTrain, carriageSpacing, frontTrain.doubleEnded));
        frontTrain.carriages.forEach(carriage -> carriage.forEachPresentEntity(
            cce -> CRPackets.PACKETS.sendTo(allPlayers, new CarriageContraptionEntityUpdatePacket(cce, frontTrain))));

        return frontTrain;
    }

    public static boolean allCarriagesLoaded(Train train) {
        for (Carriage carriage : train.carriages) {
            if (carriage.anyAvailableEntity() == null)
                return false;
        }
        return true;
    }

    public static void discardTrain(Train train) {
        train.invalid = true;
    }

    private static boolean hasBackwardControls(Carriage[] carriages) {
        return Arrays.stream(carriages).anyMatch(TrainUtils::hasBackwardControls);
    }

    private static boolean hasBackwardControls(List<Carriage> carriages) {
        return carriages.stream().anyMatch(TrainUtils::hasBackwardControls);
    }

    private static boolean hasBackwardControls(Carriage carriage) {
        CarriageContraptionEntity entity = carriage.anyAvailableEntity();
        return entity != null && entity.getContraption() instanceof CarriageContraption carriageContraption
            && carriageContraption.hasBackwardControls();
    }
}
