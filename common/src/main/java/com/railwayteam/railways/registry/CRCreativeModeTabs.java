package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.ConductorCapItem;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTab.TabVisibility;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CRCreativeModeTabs {
    @ExpectPlatform
    public static ResourceKey<CreativeModeTab> getBaseTabKey() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceKey<CreativeModeTab> getTracksTabKey() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceKey<CreativeModeTab> getPalettesTabKey() {
        throw new AssertionError();
    }

    public static void register() {
    }

    public enum Tabs {
        MAIN(CRCreativeModeTabs::getBaseTabKey),
        TRACK(CRCreativeModeTabs::getTracksTabKey),
        PALETTES(CRCreativeModeTabs::getPalettesTabKey);

        private final Supplier<ResourceKey<CreativeModeTab>> keySupplier;

        Tabs(Supplier<ResourceKey<CreativeModeTab>> keySupplier) {
            this.keySupplier = keySupplier;
        }

        public ResourceKey<CreativeModeTab> getKey() {
            return keySupplier.get();
        }
    }

    public static final class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {
        private final Tabs tab;

        public RegistrateDisplayItemsGenerator(Tabs tab) {
            this.tab = tab;
        }

        @Override
        public void accept(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
            Predicate<Item> exclusionPredicate = makeExclusionPredicate();
            List<ItemOrdering> orderings = makeOrderings();
            Function<Item, ItemStack> stackFunc = makeStackFunc();
            Function<Item, TabVisibility> visibilityFunc = makeVisibilityFunc();
            ResourceKey<CreativeModeTab> tab = this.tab.getKey();

            List<Item> items = new LinkedList<>();
            items.addAll(collectItems(tab, item -> item instanceof ConductorCapItem, true, exclusionPredicate));
            items.addAll(collectItems(tab, item -> item instanceof BlockItem, true, exclusionPredicate));
            items.addAll(collectItems(tab, item -> item instanceof BlockItem || item instanceof ConductorCapItem, false, exclusionPredicate));

            addPlatformParityItems(tab, items);
            applyOrderings(items, orderings);
            outputAll(output, items, stackFunc, visibilityFunc);
        }

        private static Predicate<Item> makeExclusionPredicate() {
            return item -> item instanceof SequencedAssemblyItem;
        }

        private static List<ItemOrdering> makeOrderings() {
            List<ItemOrdering> orderings = new ArrayList<>();

            orderings.add(ItemOrdering.after(CRBlocks.CONDUCTOR_WHISTLE_FLAG.asItem(), CRItems.ITEM_CONDUCTOR_CAP.get(DyeColor.RED).asItem()));
            orderings.add(ItemOrdering.after(CRItems.REMOTE_LENS.asItem(), CRBlocks.CONDUCTOR_WHISTLE_FLAG.asItem()));
            orderings.add(ItemOrdering.after(CRBlocks.SEMAPHORE.asItem(), CRItems.REMOTE_LENS.asItem()));
            orderings.add(ItemOrdering.after(CRItems.ITEM_BENCHCART.asItem(), CRItems.EMPTY_PAINT_PITCHER.asItem()));
            orderings.add(ItemOrdering.after(CRItems.ITEM_JUKEBOXCART.asItem(), CRItems.ITEM_BENCHCART.asItem()));
            orderings.add(ItemOrdering.after(CRBlocks.CONDUCTOR_VENT.asItem(), CRItems.ITEM_JUKEBOXCART.asItem()));

            return orderings;
        }

        private static Function<Item, ItemStack> makeStackFunc() {
            return ItemStack::new;
        }

        private static Function<Item, TabVisibility> makeVisibilityFunc() {
            Map<Item, TabVisibility> visibilities = new IdentityHashMap<>();

            for (ItemEntry<ConductorCapItem> entry : CRItems.ITEM_CONDUCTOR_CAP.values()) {
                ConductorCapItem item = entry.get();
                if (item.color != DyeColor.RED)
                    visibilities.put(item, TabVisibility.SEARCH_TAB_ONLY);
            }

            return item -> visibilities.getOrDefault(item, TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        private List<Item> collectItems(ResourceKey<CreativeModeTab> tab, Predicate<Item> classifier, boolean expected,
                                        Predicate<Item> exclusionPredicate) {
            List<Item> items = new ArrayList<>();

            for (var entry : Railways.registrate().getAll(Registries.ITEM)) {
                if (!isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get();
                if (item != Items.AIR)
                    if (classifier.test(item) == expected && !exclusionPredicate.test(item))
                        items.add(item);
            }

            return items;
        }

        private void addPlatformParityItems(ResourceKey<CreativeModeTab> tab, List<Item> items) {
            if (!tab.equals(Tabs.MAIN.getKey()))
                return;
            if (Railways.registrate().get("fuel_tank", Registries.ITEM) != null)
                return;

            addIfMissing(items, CRItems.PAINT_BRUSH.asItem());
            addIfMissing(items, CRItems.EMPTY_PAINT_PITCHER.asItem());
        }

        private static void addIfMissing(List<Item> items, Item item) {
            if (!items.contains(item))
                items.add(item);
        }

        private static void applyOrderings(List<Item> items, List<ItemOrdering> orderings) {
            for (ItemOrdering ordering : orderings) {
                int anchorIndex = items.indexOf(ordering.anchor());
                if (anchorIndex == -1)
                    continue;

                Item item = ordering.item();
                int itemIndex = items.indexOf(item);
                if (itemIndex != -1) {
                    items.remove(itemIndex);
                    if (itemIndex < anchorIndex)
                        anchorIndex--;
                }

                if (ordering.type() == ItemOrdering.Type.AFTER)
                    items.add(anchorIndex + 1, item);
                else
                    items.add(anchorIndex, item);
            }
        }

        private static void outputAll(CreativeModeTab.Output output, List<Item> items, Function<Item, ItemStack> stackFunc, Function<Item, TabVisibility> visibilityFunc) {
            for (Item item : items) {
                output.accept(stackFunc.apply(item), visibilityFunc.apply(item));
            }
        }

        private record ItemOrdering(Item item, Item anchor, ItemOrdering.Type type) {
            public static ItemOrdering after(Item item, Item anchor) {
                return new ItemOrdering(item, anchor, ItemOrdering.Type.AFTER);
            }

            public enum Type {
                AFTER
            }
        }
    }

    @ExpectPlatform
    private static boolean isInCreativeTab(com.tterrag.registrate.util.entry.RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        throw new AssertionError();
    }

    public record TabInfo(ResourceKey<CreativeModeTab> key, CreativeModeTab tab) {
    }

    public static ResourceKey<CreativeModeTab> key(String name) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, Railways.asResource(name));
    }
}
