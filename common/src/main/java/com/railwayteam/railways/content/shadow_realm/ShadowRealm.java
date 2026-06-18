package com.railwayteam.railways.content.shadow_realm;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.railwayteam.railways.Railways;
import com.zurrtum.create.content.trains.RailwaySavedData;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

public class ShadowRealm {
    public static final Logger LOGGER = LoggerFactory.getLogger(Railways.ID_NAME + "/ShadowRealm");

    @ApiStatus.Internal
    public static final UUID MARKER = UUID.fromString("b9347f13-e5b2-4519-b7a4-f34017e7080e");

    @ApiStatus.Internal
    public static Train clientShadowRestoringTrain;

    public static void banishTrain(Train train, Identifier shadowKey) throws CommandSyntaxException {
    }

    public static void handleTrainRelocationPacket(ServerPlayer sender, UUID trainId, RestorationTarget target, CallbackInfo ci) {
    }

    public static boolean restoreTrain(RailwaySavedData savedData, Train train, RestorationTarget target) {
        return false;
    }

    public record RestorationTarget(
        Level level,
        Vec3 pos,
        BezierTrackPointLocation bezier,
        Direction.AxisDirection bezierDirection,
        Vec3 lookAngle
    ) {
    }
}
