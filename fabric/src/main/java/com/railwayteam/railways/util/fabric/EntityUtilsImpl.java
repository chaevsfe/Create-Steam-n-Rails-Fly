/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.util.fabric;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.fabric.ConductorFakePlayerFabric;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Map;
import java.util.WeakHashMap;

public class EntityUtilsImpl {
    private static final Map<Entity, CompoundTag> PERSISTENT_DATA = new WeakHashMap<>();

    public static CompoundTag getPersistentData(Entity entity) {
        return PERSISTENT_DATA.computeIfAbsent(entity, $ -> new CompoundTag());
    }

    public static void givePlayerItem(Player player, ItemStack stack) {
        player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
    }

    public static ServerPlayer createConductorFakePlayer(ServerLevel level, ConductorEntity conductor) {
        return new ConductorFakePlayerFabric(level, conductor);
    }

    public static double getReachDistance(Player player) {
        return 4.5;
    }

    public static boolean handleUseEvent(Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = UseBlockCallback.EVENT.invoker().interact(player, player.level, hand, hit);
        return result != InteractionResult.FAIL;
    }
}
