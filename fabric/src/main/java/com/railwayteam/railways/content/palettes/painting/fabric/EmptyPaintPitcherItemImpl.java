package com.railwayteam.railways.content.palettes.painting.fabric;

import com.railwayteam.railways.content.palettes.painting.EmptyPaintPitcherItem;
import net.minecraft.world.item.Item;

public class EmptyPaintPitcherItemImpl extends EmptyPaintPitcherItem {
    public EmptyPaintPitcherItemImpl(Properties properties) {
        super(properties);
    }

    public static EmptyPaintPitcherItem create(Item.Properties properties) {
        return new EmptyPaintPitcherItemImpl(properties);
    }
}
