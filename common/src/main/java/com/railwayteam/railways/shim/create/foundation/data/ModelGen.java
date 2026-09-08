package com.railwayteam.railways.shim.create.foundation.data;

import com.railwayteam.railways.shim.registrate.builders.ItemBuilder;
import com.railwayteam.railways.shim.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.item.Item;

public class ModelGen {
    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> customItemModel() {
        return NonNullUnaryOperator.identity();
    }

    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> customItemModel(String path) {
        return NonNullUnaryOperator.identity();
    }

    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> customItemModel(String prefix, String suffix) {
        return NonNullUnaryOperator.identity();
    }
}
