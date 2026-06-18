package com.railwayteam.railways.content.cycle_menu;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagCycleTracker {
    private final List<TagKey<Item>> cyclingTags = new ArrayList<>();
    private final Map<TagKey<Item>, List<Item>> cycles = new HashMap<>();
    private final Map<Item, TagKey<Item>> reverseLookup = new HashMap<>();
    private boolean cyclesComputed = false;

    public void registerCycle(TagKey<Item> tag) {
        if (!cyclingTags.contains(tag))
            cyclingTags.add(tag);
    }

    public void scheduleRecompute() {
        cyclesComputed = false;
    }

    public void computeCycles() {
        cycles.clear();
        reverseLookup.clear();
        cyclingTags.forEach(tag -> cycles.put(tag, new ArrayList<>()));

        for (Item item : BuiltInRegistries.ITEM) {
            for (TagKey<Item> tag : cyclingTags) {
                if (item.builtInRegistryHolder().is(tag)) {
                    cycles.get(tag).add(item);
                    reverseLookup.put(item, tag);
                }
            }
        }
        cyclesComputed = true;
    }

    public @Nullable TagKey<Item> getCycleTag(Item item) {
        if (!cyclesComputed)
            computeCycles();
        TagKey<Item> tag = reverseLookup.get(item);
        if (tag != null && cycles.get(tag).size() == 1)
            return null;
        return tag;
    }

    public List<Item> getCycle(TagKey<Item> tag) {
        if (!cyclesComputed)
            computeCycles();
        return cycles.getOrDefault(tag, ImmutableList.of());
    }
}
