package com.railwayteam.railways.content.coupling;

import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TrainUtils {
    public static Train splitTrain(Train train, int numberOffEnd) {
        return train;
    }

    public static void tryToParkNearby(Train train, double maxDistance) {
    }

    public static Train combineTrains(Train frontTrain, Train backTrain, BlockPos itemDropPos, Level itemDropLevel, int carriageSpacing) {
        return frontTrain;
    }

    public static Train combineTrains(Train frontTrain, Train backTrain, Vec3 itemDropPos, Level itemDropLevel, int carriageSpacing) {
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
}
