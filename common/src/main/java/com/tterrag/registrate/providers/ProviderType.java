package com.tterrag.registrate.providers;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ProviderType<T> {
    public static final ProviderType<RegistrateTagsProvider<Block>> BLOCK_TAGS = new ProviderType<>();
    public static final ProviderType<RegistrateTagsProvider<Item>> ITEM_TAGS = new ProviderType<>();
    public static final ProviderType<Object> LANG = new ProviderType<>();
}
