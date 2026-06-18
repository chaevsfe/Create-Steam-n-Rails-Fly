package com.tterrag.registrate.util.entry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemProviderEntry<T extends Item> extends RegistryEntry<T> implements ItemLike {
    public ItemProviderEntry(Identifier id, T value) {
        super(id, value);
    }

    @Override
    public T asItem() {
        return get();
    }

    public ItemStack asStack() {
        return new ItemStack(get());
    }
}
