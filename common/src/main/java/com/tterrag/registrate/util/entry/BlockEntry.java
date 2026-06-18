package com.tterrag.registrate.util.entry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntry<T extends Block> extends RegistryEntry<T> implements ItemLike {
    public BlockEntry(Identifier id, T value) {
        super(id, value);
    }

    @Override
    public Item asItem() {
        return get().asItem();
    }

    public ItemStack asStack() {
        return new ItemStack(asItem());
    }

    public BlockState getDefaultState() {
        return get().defaultBlockState();
    }

    public boolean has(BlockState state) {
        return state != null && state.is(get());
    }

    public boolean isIn(ItemStack stack) {
        return stack != null && stack.is(asItem());
    }
}
