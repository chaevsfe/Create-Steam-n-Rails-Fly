package com.railwayteam.railways.content.palettes.painting.fabric;

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public class PaintPitcherItemImpl extends PaintPitcherItem {
    public PaintPitcherItemImpl(Properties properties, @Nullable PalettesColor color) {
        super(properties, color);
    }

    public static PaintPitcherItem create(Item.Properties properties, @Nullable PalettesColor color) {
        return new PaintPitcherItemImpl(properties, color);
    }
}
