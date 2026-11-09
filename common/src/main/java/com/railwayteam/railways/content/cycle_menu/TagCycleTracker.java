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
