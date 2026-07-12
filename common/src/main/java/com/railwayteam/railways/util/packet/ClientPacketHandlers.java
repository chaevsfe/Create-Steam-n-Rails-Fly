/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.ConductorPossessionController;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlockEntity;
import com.railwayteam.railways.content.distant_signals.IOverridableSignal;
import com.railwayteam.railways.content.minecarts.MinecartJukebox;
import com.railwayteam.railways.mixin.AccessorCarriageContraptionEntity;
import com.railwayteam.railways.mixin.AccessorTrain;
import com.railwayteam.railways.mixin_interfaces.IUpdateCount;
import com.zurrtum.create.client.CreateClient;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * S2CPacket.handle(Minecraft) implementations that touch Minecraft.level (declared type
 * ClientLevel, a client-only Create Fly/vanilla type) or other client-only Minecraft fields.
 * Kept in one class, separate from the packet classes themselves, so the packet classes'
 * own bytecode never embeds those types in a method - a class's own methods are fully verified
 * (and their referenced types resolved) the instant the class loads, on either side, regardless
 * of any @Environment annotation on that one method or any runtime guard around invoking it.
 */
@Environment(EnvType.CLIENT)
public class ClientPacketHandlers {
    private static final int MAX_CAMERA_LOOKUP_TICKS = 100;
    private static int pendingCameraEntityId = -1;
    private static int pendingCameraLookupTicks;

    public static void handleCameraMove(Minecraft mc, int id, double x, double y, double z, float yaw, float pitch, boolean onGround) {
        if (mc.level == null) return;
        if (!(mc.level.getEntity(id) instanceof ConductorEntity conductor)) return;
        if (mc.getCameraEntity() != conductor) return;

        conductor.syncPacketPositionCodec(x, y, z);
        conductor.setPos(x, y, z);
        conductor.setYRot(yaw);
        conductor.setXRot(pitch);
        conductor.setOnGround(onGround);
    }

    public static void handleJukeboxCart(Minecraft mc, int id, ItemStack record) {
        Level level = mc.level;
        if (level == null)
            return;

        Entity target = level.getEntity(id);
        if (target instanceof MinecartJukebox juke)
            juke.insertRecord(record);
    }

    public static void handleMountedToolboxSync(Minecraft mc, int id, CompoundTag nbt) {
        Level level = mc.level;
        if (level != null) {
            Entity target = level.getEntity(id);
            if (target instanceof ConductorEntity conductor) {
                conductor.getOrCreateToolboxHolder().read(nbt, true);
            }
        }
    }

    // Create's own train-sync packets (e.g. AddTrainPacket, used to register a newly split-off
    // train client-side) travel over a different network channel than our own S2C packets, with
    // no guaranteed relative ordering once received. If one of our packets referencing a train by
    // UUID arrives before Create's packet has registered that train, retry on a later tick instead
    // of silently desyncing the client's copy of the train from the server's.
    private static final int MAX_TRAIN_LOOKUP_RETRIES = 60;

    public static void handleCarriageContraptionEntityUpdate(Minecraft mc, int id, UUID trainId, int carriageIndex) {
        handleCarriageContraptionEntityUpdate(mc, id, trainId, carriageIndex, MAX_TRAIN_LOOKUP_RETRIES);
    }

    private static void handleCarriageContraptionEntityUpdate(Minecraft mc, int id, UUID trainId, int carriageIndex, int retriesLeft) {
        Level level = mc.level;
        if (level == null)
            return;
        Train train = CreateClient.RAILWAYS().trains.get(trainId);
        if ((train == null || train.carriages.size() <= carriageIndex)) {
            if (retriesLeft > 0) {
                mc.execute(() -> handleCarriageContraptionEntityUpdate(mc, id, trainId, carriageIndex, retriesLeft - 1));
            } else {
                Railways.LOGGER.warn("[ClientPacketHandlers] carriageEntityUpdate gave up: train {} never appeared for entity {}", trainId, id);
            }
            return;
        }
        Entity target = level.getEntity(id);
        if (target instanceof CarriageContraptionEntity cce) {
            cce.trainId = trainId;
            ((AccessorCarriageContraptionEntity) cce).railways$setCarriage(null);
            cce.carriageIndex = carriageIndex;
            ((AccessorCarriageContraptionEntity) cce).railways$bindCarriage();
            ((IUpdateCount) cce).railways$markUpdate();
        }
    }

    public static void handleChopTrainEnd(Minecraft mc, UUID trainId, int numberOfCarriages, boolean doubleEnded) {
        Level level = mc.level;
        if (level != null) {
            Train train = CreateClient.RAILWAYS().trains.get(trainId);
            if (train != null) {
                for (int i = 0; i < numberOfCarriages; i++) {
                    train.carriages.remove(train.carriages.size() - 1);
                    if (!train.carriageSpacing.isEmpty())
                        train.carriageSpacing.remove(train.carriageSpacing.size() - 1);
                }
                double[] originalStress = ((AccessorTrain) train).railways$getStress();
                double[] newStress = new double[originalStress.length - numberOfCarriages];
                System.arraycopy(originalStress, 0, newStress, 0, newStress.length);
                ((AccessorTrain) train).railways$setStress(newStress);
                train.doubleEnded = doubleEnded;
            }
        }
    }

    public static void handleAddTrainEnd(Minecraft mc, UUID trainId, UUID backTrainId, int middleSpacing, boolean doubleEnded) {
        handleAddTrainEnd(mc, trainId, backTrainId, middleSpacing, doubleEnded, MAX_TRAIN_LOOKUP_RETRIES);
    }

    private static void handleAddTrainEnd(Minecraft mc, UUID trainId, UUID backTrainId, int middleSpacing, boolean doubleEnded, int retriesLeft) {
        Level level = mc.level;
        if (level != null) {
            Train train = CreateClient.RAILWAYS().trains.get(trainId);
            Train backTrain = CreateClient.RAILWAYS().trains.get(backTrainId);
            if (train == null || backTrain == null) {
                if (retriesLeft > 0) {
                    mc.execute(() -> handleAddTrainEnd(mc, trainId, backTrainId, middleSpacing, doubleEnded, retriesLeft - 1));
                } else {
                    Railways.LOGGER.warn("[ClientPacketHandlers] addTrainEnd gave up: trainFound={} backTrainFound={}", train != null, backTrain != null);
                }
                return;
            }
            train.carriages.addAll(backTrain.carriages);
            backTrain.carriages.clear();

            train.carriageSpacing.add(middleSpacing);
            train.carriageSpacing.addAll(backTrain.carriageSpacing);
            backTrain.carriageSpacing.clear();

            double[] newStress = new double[((AccessorTrain) train).railways$getStress().length + ((AccessorTrain) backTrain).railways$getStress().length + 1];
            System.arraycopy(((AccessorTrain) train).railways$getStress(), 0, newStress, 0, ((AccessorTrain) train).railways$getStress().length);
            newStress[((AccessorTrain) train).railways$getStress().length] = 0;
            System.arraycopy(((AccessorTrain) backTrain).railways$getStress(), 0, newStress, ((AccessorTrain) train).railways$getStress().length + 1, ((AccessorTrain) backTrain).railways$getStress().length);
            ((AccessorTrain) train).railways$setStress(newStress);
            train.doubleEnded = doubleEnded;

            train.carriages.forEach(c -> c.setTrain(train));

            CreateClient.RAILWAYS().trains.remove(backTrainId);
        }
    }

    public static void handleTrackCouplerClientInfo(Minecraft mc, BlockPos blockPos, TrackCouplerBlockEntity.ClientInfo info) {
        Level level = mc.level;
        if (level != null) {
            BlockEntity te = level.getBlockEntity(blockPos);
            if (te instanceof TrackCouplerBlockEntity couplerTile)
                couplerTile.setClientInfo(info);
        }
    }

    public static void handleOverridableSignal(Minecraft mc, BlockPos blockPos, @Nullable BlockPos signalPos, SignalBlockEntity.SignalState signalState, int ticks, boolean distantSignal) {
        Level level = mc.level;
        if (level != null) {
            BlockEntity te = level.getBlockEntity(blockPos);
            if (te instanceof IOverridableSignal overridableSignal) {
                SignalBlockEntity signalBE = null;
                if (signalPos != null && level.getBlockEntity(signalPos) instanceof SignalBlockEntity signal)
                    signalBE = signal;
                overridableSignal.railways$refresh(signalBE, signalState, ticks, distantSignal);
            }
        }
    }

    public static void handleSetCameraView(Minecraft mc, int id, boolean conductorCamera) {
        pendingCameraEntityId = -1;
        pendingCameraLookupTicks = 0;
        if (mc.level == null) {
            if (conductorCamera) {
                pendingCameraEntityId = id;
                pendingCameraLookupTicks = MAX_CAMERA_LOOKUP_TICKS;
            }
            return;
        }

        Entity entity = mc.level.getEntity(id);
        if (entity == null && conductorCamera) {
            // A far-away conductor's spawn packet can arrive after the camera command. Keep the
            // request until native chunk/entity tracking has populated the client level.
            pendingCameraEntityId = id;
            pendingCameraLookupTicks = MAX_CAMERA_LOOKUP_TICKS;
            return;
        }

        applyCameraView(mc, entity, conductorCamera);
    }

    public static void retryPendingCameraView(Minecraft mc) {
        if (pendingCameraEntityId < 0)
            return;
        if (mc.level == null) {
            if (--pendingCameraLookupTicks <= 0)
                pendingCameraEntityId = -1;
            return;
        }

        Entity entity = mc.level.getEntity(pendingCameraEntityId);
        if (entity instanceof ConductorEntity) {
            pendingCameraEntityId = -1;
            pendingCameraLookupTicks = 0;
            applyCameraView(mc, entity, true);
            return;
        }

        if (--pendingCameraLookupTicks <= 0) {
            Railways.LOGGER.warn("Conductor camera entity {} did not arrive after {} client ticks; dismounting safely",
                    pendingCameraEntityId, MAX_CAMERA_LOOKUP_TICKS);
            pendingCameraEntityId = -1;
            ConductorPossessionController.dismount();
        }
    }

    private static void applyCameraView(Minecraft mc, @Nullable Entity entity, boolean conductorCamera) {
        boolean isCamera = conductorCamera && entity instanceof ConductorEntity;

        if (isCamera || entity instanceof Player) {
            mc.setCameraEntity(entity);

            if (isCamera) {
                if (ConductorPossessionController.previousCameraType == null)
                    ConductorPossessionController.previousCameraType = mc.options.getCameraType();
                mc.options.setCameraType(CameraType.FIRST_PERSON);
                ConductorPossessionController.setRenderPosition(entity);
                if (mc.player != null) {
                    mc.player.xxa = 0.0f;
                    mc.player.zza = 0.0f;
                    mc.player.setJumping(false);
                }
            }
            else if (ConductorPossessionController.previousCameraType != null) {
                mc.options.setCameraType(ConductorPossessionController.previousCameraType);
                ConductorPossessionController.previousCameraType = null;
            }

            mc.levelExtractor.allChanged();
        }
    }
}
