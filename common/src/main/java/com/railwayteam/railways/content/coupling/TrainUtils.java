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
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.packet.s2c.AddTrainPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TrainUtils {
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

        return newTrain;
    }

    public static void tryToParkNearby(Train train, double maxDistance) {
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
