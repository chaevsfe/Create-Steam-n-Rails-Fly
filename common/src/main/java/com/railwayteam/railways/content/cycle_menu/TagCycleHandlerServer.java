package com.railwayteam.railways.content.cycle_menu;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.annotation.multiloader.MultiLoaderEvent;
import com.railwayteam.railways.content.smokestack.SmokestackStyle;
import com.railwayteam.railways.registry.CRPalettes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TagCycleHandlerServer {
    public static final TagCycleTracker CYCLE_TRACKER = new TagCycleTracker();

    static {
        registerDefaultCycles();
    }

    private static void registerDefaultCycles() {
        CRPalettes.CYCLE_GROUPS.values().forEach(CYCLE_TRACKER::registerCycle);
        CYCLE_TRACKER.registerCycle(SmokestackStyle.variantToTagKey("caboosestyle"));
        CYCLE_TRACKER.registerCycle(SmokestackStyle.variantToTagKey("long"));
        CYCLE_TRACKER.registerCycle(SmokestackStyle.variantToTagKey("coalburner"));
        CYCLE_TRACKER.registerCycle(SmokestackStyle.variantToTagKey("oilburner"));
        CYCLE_TRACKER.registerCycle(SmokestackStyle.variantToTagKey("streamlined"));
        CYCLE_TRACKER.registerCycle(SmokestackStyle.variantToTagKey("woodburner"));
        CYCLE_TRACKER.scheduleRecompute();
    }

    private static boolean select(ServerPlayer player, Item target, InteractionHand hand) {
        ItemStack handStack = player.getItemInHand(hand);
        if (handStack.is(target))
            return true;
        TagKey<Item> handTag = CYCLE_TRACKER.getCycleTag(handStack.getItem());
        TagKey<Item> targetTag = CYCLE_TRACKER.getCycleTag(target);
        if (handTag == null || !handTag.equals(targetTag))
            return false;
        player.setItemInHand(hand, handStack.transmuteCopy(target, handStack.getCount()));
        return true;
    }

    public static void select(ServerPlayer player, Item target) {
        if (select(player, target, InteractionHand.MAIN_HAND) || select(player, target, InteractionHand.OFF_HAND))
            return;
        Railways.LOGGER.debug("Player {} tried to select {} through tag cycling but no held item matched",
            player.getScoreboardName(), BuiltInRegistries.ITEM.getKey(target));
    }

    @MultiLoaderEvent
    public static void onTagsUpdated() {
        CYCLE_TRACKER.scheduleRecompute();
    }
}
