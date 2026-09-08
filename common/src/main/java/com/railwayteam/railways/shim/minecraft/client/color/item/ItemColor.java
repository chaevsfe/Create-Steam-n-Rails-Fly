package com.railwayteam.railways.shim.minecraft.client.color.item;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemColor {
    int getColor(ItemStack stack, int tintIndex);
}
