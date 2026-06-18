package com.railwayteam.railways.util.fabric;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.fabric.ConductorFakePlayerFabric;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
        player.getInventory().placeItemBackInInventory(stack);
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
