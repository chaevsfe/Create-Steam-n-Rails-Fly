/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxHandlerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(value = ToolboxHandlerClient.class, remap = false)
public class MixinToolboxHandlerClient {
    @Unique
    private static ConductorEntity railways$getConductorForSlot(int slot) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null)
            return null;

        CompoundTag data = AllSynchedDatas.TOOLBOX.get(player)
            .getCompound(String.valueOf(slot))
            .orElse(new CompoundTag());
        UUID uuid;
        try {
            uuid = data.getString("EntityUUID").map(UUID::fromString).orElse(null);
        } catch (IllegalArgumentException ignored) {
            uuid = null;
        }
        if (uuid == null)
            return null;

        for (ConductorEntity conductor : ConductorEntity.WITH_TOOLBOXES.get(mc.level))
            if (conductor.getUUID().equals(uuid))
                return conductor;
        return null;
    }

    @Unique
    @Nullable
    private static ConductorEntity railways$conductorForSelectedSlot;

    @ModifyVariable(
        method = "onKeyInput",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/equipment/toolbox/ToolboxHandler;getMaxRange(Lnet/minecraft/world/entity/player/Player;)D"
        )
    )
    @SuppressWarnings("DataFlowIssue")
    private static BlockPos railways$useConductorToolboxDistance(BlockPos pos) {
        ConductorEntity conductor = railways$getConductorForSlot(Minecraft.getInstance().player.getInventory().getSelectedSlot());
        railways$conductorForSelectedSlot = conductor;
        return conductor == null ? pos : conductor.blockPosition();
    }

    @ModifyVariable(
        method = "onKeyInput",
        remap = false,
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
            remap = true
        )
    )
    @SuppressWarnings("InvalidInjectorMethodSignature")
    private static BlockEntity railways$getConductorToolbox(BlockEntity be) {
        ConductorEntity conductor = railways$conductorForSelectedSlot;
        return conductor == null ? be : conductor.getToolbox();
    }

    @Unique
    private static int railways$currentRenderedSlot;

    @ModifyArg(
        method = "renderOverlay",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"
        )
    )
    private static int railways$grabSlot(int slot) {
        railways$currentRenderedSlot = slot;
        return slot;
    }

    @ModifyVariable(
        method = "renderOverlay",
        require = 0,
        remap = false,
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/nbt/NbtUtils;readBlockPos(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/core/BlockPos;",
            remap = true
        )
    )
    @SuppressWarnings("InvalidInjectorMethodSignature")
    private static BlockPos railways$useConductorToolboxForBackground(BlockPos pos) {
        ConductorEntity conductor = railways$getConductorForSlot(railways$currentRenderedSlot);
        return conductor == null ? pos : conductor.blockPosition();
    }
}
