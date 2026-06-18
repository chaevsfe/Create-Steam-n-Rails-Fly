package com.railwayteam.railways.content.cycle_menu;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.annotation.multiloader.MultiLoaderEvent;
import com.railwayteam.railways.content.smokestack.SmokestackStyle;
import com.railwayteam.railways.registry.CRKeys;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.registry.CRPalettes;
import com.railwayteam.railways.util.EntityUtils;
import com.railwayteam.railways.util.packet.TagCycleSelectionPacket;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TagCycleHandlerClient {
    public static final TagCycleTracker CYCLE_TRACKER = new TagCycleTracker();
    private static final boolean DEBUG = Boolean.getBoolean("railways.debugCycleMenu");
    private static int debugCooldown = 0;
    @ApiStatus.Internal
    public static int COOLDOWN = 0;

    static {
        registerDefaultCycles();
        debug("debug probe enabled; registered palette cycle groups={}", CRPalettes.CYCLE_GROUPS.size());
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

    @MultiLoaderEvent
    public static void clientTick() {
        if (COOLDOWN > 0 && !CRKeys.CYCLE_MENU.isPressed())
            COOLDOWN--;
        debugTick();
        if (COOLDOWN <= 0 && CRKeys.CYCLE_MENU.isPressed())
            openCycleMenu();
    }

    static void select(Item target) {
        CRPackets.PACKETS.send(new TagCycleSelectionPacket(target));
    }

    @MultiLoaderEvent
    public static void onKeyInput(int key, boolean pressed) {
        if (key != CRKeys.CYCLE_MENU.getBoundCode() || !pressed || COOLDOWN > 0)
            return;
        openCycleMenu();
    }

    private static void openCycleMenu() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            debug("not opening: screen already present ({})", mc.screen.getClass().getName());
            return;
        }
        if (mc.gameMode == null) {
            debug("not opening: gameMode is null");
            return;
        }
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            debug("not opening: player is spectator");
            return;
        }

        LocalPlayer player = mc.player;
        if (player == null) {
            debug("not opening: player is null");
            return;
        }

        CycleSelection selection = findSelection(player);
        if (selection == null) {
            debug("not opening: no cycle tag found for held items; main={}, off={}",
                describe(player.getItemInHand(InteractionHand.MAIN_HAND)),
                describe(player.getItemInHand(InteractionHand.OFF_HAND)));
            return;
        }

        debug("opening menu: tag={}, cycleSize={}, template={}",
            selection.tag().location(), CYCLE_TRACKER.getCycle(selection.tag()).size(), describe(selection.stack()));
        ScreenOpener.open(new RadialTagCycleMenu(selection.tag(), CYCLE_TRACKER.getCycle(selection.tag()), selection.stack()));
    }

    @MultiLoaderEvent
    public static void onTagsUpdated() {
        CYCLE_TRACKER.scheduleRecompute();
    }

    @Nullable
    private static CycleSelection findSelection(LocalPlayer player) {
        final CycleSelection[] selection = new CycleSelection[1];
        EntityUtils.isHolding(player, stack -> {
            TagKey<Item> tag = CYCLE_TRACKER.getCycleTag(stack.getItem());
            if (tag == null)
                return false;
            selection[0] = new CycleSelection(tag, stack.copyWithCount(1));
            return true;
        });
        return selection[0];
    }

    private record CycleSelection(TagKey<Item> tag, ItemStack stack) {
    }

    private static void debugTick() {
        if (!DEBUG)
            return;
        if (debugCooldown > 0) {
            debugCooldown--;
            return;
        }
        debugCooldown = CRKeys.CYCLE_MENU.isPressed() || CRKeys.altDown() ? 20 : 100;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        debug("tick: cyclePressed={}, altDown={}, boundKey={}, boundCode={}, cooldown={}, screen={}, main={}, off={}",
            CRKeys.CYCLE_MENU.isPressed(),
            CRKeys.altDown(),
            CRKeys.CYCLE_MENU.getBoundKey(),
            CRKeys.CYCLE_MENU.getBoundCode(),
            COOLDOWN,
            mc.screen == null ? "null" : mc.screen.getClass().getName(),
            player == null ? "no player" : describe(player.getItemInHand(InteractionHand.MAIN_HAND)),
            player == null ? "no player" : describe(player.getItemInHand(InteractionHand.OFF_HAND)));
    }

    private static String describe(ItemStack stack) {
        if (stack.isEmpty())
            return "empty";
        TagKey<Item> tag = CYCLE_TRACKER.getCycleTag(stack.getItem());
        String tagDescription = tag == null ? "no cycle tag" : tag.location() + " size=" + CYCLE_TRACKER.getCycle(tag).size();
        return BuiltInRegistries.ITEM.getKey(stack.getItem()) + " x" + stack.getCount() + " (" + tagDescription + ")";
    }

    private static void debug(String message, Object... args) {
        if (DEBUG)
            Railways.LOGGER.info("[CycleMenu] " + message, args);
    }
}
