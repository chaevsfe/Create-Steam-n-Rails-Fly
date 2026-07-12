package com.railwayteam.railways.content.shadow_realm;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.mixin.AccessorCarriage;
import com.railwayteam.railways.mixin.AccessorGlobalRailwayManager;
import com.railwayteam.railways.mixin_interfaces.IShadowTrain;
import com.railwayteam.railways.mixin_interfaces.RailwaySavedDataDuck;
import com.railwayteam.railways.multiloader.PlayerSelection;
import com.railwayteam.railways.registry.CRPackets;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.RailwaySavedData;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Carriage.DimensionalCarriageEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.entity.TrainRelocator;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import com.zurrtum.create.infrastructure.packet.s2c.AddTrainPacket;
import com.zurrtum.create.infrastructure.packet.s2c.ContraptionRelocationPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
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

    @ApiStatus.Internal
    public static UUID clientPendingShadowTrainId;

    private static final DynamicCommandExceptionType DUPLICATE_KEY = new DynamicCommandExceptionType(
        key -> Component.literal("Shadow key '" + key + "' is already in use")
    );

    public static void banishTrain(Train train, Identifier shadowKey) throws CommandSyntaxException {
        IShadowTrain shadowTrain = (IShadowTrain) train;
        if (shadowTrain.railways$isShadow()) {
            return;
        }

        RailwaySavedData savedData = ((AccessorGlobalRailwayManager) Create.RAILWAYS).railways$getSavedData();
        if (savedData == null) {
            throw new IllegalStateException("Railway saved data is unavailable while banishing a train");
        }

        RailwaySavedDataDuck shadowData = (RailwaySavedDataDuck) savedData;
        boolean pendingDuplicate = Create.RAILWAYS.trains.values().stream()
            .filter(candidate -> candidate != train)
            .map(candidate -> ((IShadowTrain) candidate).railways$getShadowKey())
            .anyMatch(shadowKey::equals);
        if (shadowData.railways$getShadowKeys().containsKey(shadowKey) || pendingDuplicate) {
            throw DUPLICATE_KEY.create(shadowKey);
        }

        shadowTrain.railways$setShadow(shadowKey);
        for (Carriage carriage : train.carriages) {
            for (DimensionalCarriageEntity entity : ((AccessorCarriage) carriage).railways$getEntities().values()) {
                // The dimensional-carriage mixin makes every occupied seat fall outside the loaded slice,
                // causing passengers to be captured into the carriage before its entity is removed.
                entity.updatePassengerLoadout();
            }
        }

        train.navigation.cancelNavigation();
        train.speed = 0;
        train.derailed = true;
        train.graph = null;
        train.status.displayInformation("railways.shadow_realm.banished", true);
    }

    public static void handleTrainRelocationPacket(ServerPlayer sender, UUID trainId, RestorationTarget target, CallbackInfo ci) {
        RailwaySavedData savedData = ((AccessorGlobalRailwayManager) Create.RAILWAYS).railways$getSavedData();
        if (savedData == null) {
            LOGGER.warn("Received TrainRelocationPacket but railway saved data was unavailable");
            ci.cancel();
            return;
        }

        Train shadowTrain = ((RailwaySavedDataDuck) savedData).railway$getShadowTrains().get(trainId);
        if (shadowTrain == null) {
            // A real train is handled by Create. An unknown id is consumed here to avoid Create's
            // null-train error path and cannot be a legitimate relocation request.
            if (!Create.RAILWAYS.trains.containsKey(trainId)) {
                LOGGER.warn("Received TrainRelocationPacket for unknown train id {}", trainId);
                ci.cancel();
            }
            return;
        }

        ci.cancel();
        String messagePrefix = sender.getName().getString() + " could not restore Train "
            + shadowTrain.name.getString();

        if (!sender.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            LOGGER.warn("{}: player has insufficient permissions", messagePrefix);
            return;
        }

        int verifyDistance = AllConfigs.server().trains.maxTrackPlacementLength.get() * 2;
        if (!sender.position().closerThan(Vec3.atCenterOf(target.pos()), verifyDistance)) {
            LOGGER.warn("{}: player too far from clicked position", messagePrefix);
            return;
        }

        if (restoreTrain(savedData, shadowTrain, target)) {
            sender.sendOverlayMessage(Component.translatable("create.train.relocate.success")
                .withStyle(ChatFormatting.GREEN));
            LOGGER.info("{} restored '{}' from the shadow realm", sender.getName().getString(),
                shadowTrain.name.getString());
            return;
        }

        LOGGER.warn("{}: restoration failed server-side", messagePrefix);
    }

    public static boolean restoreTrain(RailwaySavedData savedData, Train train, RestorationTarget target) {
        IShadowTrain shadowTrain = (IShadowTrain) train;
        if (!shadowTrain.railways$isShadow()) {
            return true;
        }
        if (!target.apply(train)) {
            return false;
        }

        RailwaySavedDataDuck shadowData = (RailwaySavedDataDuck) savedData;
        shadowData.railway$getShadowTrains().remove(train.id);
        shadowData.railways$getShadowKeys().remove(shadowTrain.railways$getShadowKey());

        shadowTrain.railways$clearShadow();
        Create.RAILWAYS.addTrain(train);
        savedData.setDirty();

        CRPackets.PACKETS.sendTo(PlayerSelection.all(), new AddTrainPacket(train));
        train.status.displayInformation("railways.shadow_realm.restored", true);
        return true;
    }

    public record RestorationTarget(
        Level level,
        BlockPos pos,
        BezierTrackPointLocation bezier,
        boolean bezierDirection,
        Vec3 lookAngle
    ) {
        public boolean apply(Train train) {
            if (!TrainRelocator.relocate(train, level, pos, bezier, bezierDirection, lookAngle, null)) {
                return false;
            }

            train.carriages.forEach(carriage -> carriage.forEachPresentEntity(entity -> {
                entity.nonDamageTicks = 10;
                if (entity.level() instanceof ServerLevel serverLevel) {
                    serverLevel.getChunkSource()
                        .sendToTrackingPlayers(entity, new ContraptionRelocationPacket(entity.getId()));
                }
            }));
            return true;
        }
    }
}
