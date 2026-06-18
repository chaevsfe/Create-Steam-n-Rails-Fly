/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(value = ToolboxHandler.class, remap = false)
public abstract class MixinToolboxHandler {
    @Shadow
    public static void syncData(Player player, CompoundTag data) {
        throw new AssertionError();
    }

    @Inject(method = { "onLoad", "onUnload" }, at = @At("HEAD"), cancellable = true)
    private static void railways$keepMountedToolboxesOutOfMap(ToolboxBlockEntity be, CallbackInfo ci) {
        if (be instanceof MountedToolbox)
            ci.cancel();
    }

    @Inject(method = "entityTick", at = @At("TAIL"))
    private static void railways$connectMountedToolboxes(Entity entity, Level world, CallbackInfo ci) {
        if (world.isClientSide() || !(world instanceof ServerLevel level) || !(entity instanceof ServerPlayer player))
            return;
        if (entity.tickCount % 20 != 0)
            return;

        CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);
        boolean sendData = false;
        for (int i = 0; i < 9; i++) {
            String key = String.valueOf(i);
            CompoundTag data = compound.getCompound(key).orElse(null);
            if (data == null)
                continue;

            UUID uuid = railways$getEntityUUID(data);
            if (uuid == null)
                continue;

            Entity toolboxHolder = level.getEntity(uuid);
            if (toolboxHolder instanceof ConductorEntity conductor && conductor.isCarryingToolbox()) {
                conductor.getToolbox().connectPlayer(data.getInt("Slot").orElse(0), player, i);
                continue;
            }

            compound.remove(key);
            sendData = true;
        }

        if (sendData)
            syncData(player, compound);
    }

    @Inject(method = "unequip", at = @At("HEAD"), cancellable = true)
    private static void railways$unequipMountedToolbox(Player player, int hotbarSlot, boolean keepItems, CallbackInfo ci) {
        if (!(player.level() instanceof ServerLevel level))
            return;

        CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);
        String key = String.valueOf(hotbarSlot);
        CompoundTag data = compound.getCompound(key).orElse(null);
        if (data == null)
            return;

        UUID uuid = railways$getEntityUUID(data);
        if (uuid == null)
            return;

        Entity entity = level.getEntity(uuid);
        if (!(entity instanceof ConductorEntity conductor) || !conductor.isCarryingToolbox())
            return;

        MountedToolbox toolbox = conductor.getToolbox();
        double maxRange = ToolboxHandler.getMaxRange(player);
        toolbox.unequip(data.getInt("Slot").orElse(0), player, hotbarSlot,
            keepItems || player.distanceToSqr(conductor) >= maxRange * maxRange);
        compound.remove(key);
        ci.cancel();
    }

    @Inject(method = "getNearest", at = @At("RETURN"), cancellable = true)
    private static void railways$findNearbyMountedToolboxes(LevelAccessor world, Player player, int maxAmount,
                                                           CallbackInfoReturnable<List<ToolboxBlockEntity>> cir) {
        Set<ConductorEntity> conductors = ConductorEntity.WITH_TOOLBOXES.get(world);
        if (conductors.isEmpty())
            return;

        Vec3 playerPos = player.position();
        double maxRangeSqr = Math.pow(ToolboxHandler.getMaxRange(player), 2);
        List<ToolboxBlockEntity> toolboxes = new ArrayList<>(cir.getReturnValue());
        for (ConductorEntity conductor : conductors) {
            if (!conductor.isCarryingToolbox())
                continue;
            if (player.distanceToSqr(conductor) < maxRangeSqr)
                toolboxes.add(conductor.getToolbox());
        }

        toolboxes.sort(Comparator.comparingDouble(toolbox -> railways$distance(playerPos, toolbox)));
        if (toolboxes.size() > maxAmount)
            toolboxes = new ArrayList<>(toolboxes.subList(0, maxAmount));
        cir.setReturnValue(toolboxes);
    }

    @Unique
    private static double railways$distance(Vec3 playerPos, ToolboxBlockEntity toolbox) {
        if (toolbox instanceof MountedToolbox mounted)
            return playerPos.distanceToSqr(mounted.getParent().position());
        return ToolboxHandler.distance(playerPos, toolbox.getBlockPos());
    }

    @Unique
    private static UUID railways$getEntityUUID(CompoundTag data) {
        try {
            return data.getString("EntityUUID").map(UUID::fromString).orElse(null);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
