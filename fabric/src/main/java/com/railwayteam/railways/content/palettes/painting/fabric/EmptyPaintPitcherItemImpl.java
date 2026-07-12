package com.railwayteam.railways.content.palettes.painting.fabric;

import com.railwayteam.railways.content.palettes.painting.EmptyPaintPitcherItem;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.item.Item;

public class EmptyPaintPitcherItemImpl extends EmptyPaintPitcherItem {
    public EmptyPaintPitcherItemImpl(Properties properties) {
        super(properties);

        FluidStorage.ITEM.registerForItems(($, context) -> new PaintPitcherFluidStorage(context), this);
    }

    public static EmptyPaintPitcherItem create(Item.Properties properties) {
        return new EmptyPaintPitcherItemImpl(properties);
    }
}
