package com.tterrag.registrate.util.entry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class ItemEntry<T extends Item> extends ItemProviderEntry<T> {
    public ItemEntry(Identifier id, T value) {
        super(id, value);
    }

    public static <T extends Item> ItemEntry<T> cast(RegistryEntry<?> entry) {
        return new ItemEntry<>(entry.getIdentifier(), (T) entry.get());
    }
}
