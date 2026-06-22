package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class CRIcons extends AllIcons {
    public static final Identifier ICON_ATLAS = Railways.asResource("textures/gui/icons.png");

    private static int x = 0;
    private static int y = -1;

    private final int crIconX;
    private final int crIconY;

    public static final CRIcons I_SEARCH_DOWN = newRow(), I_SEARCH_UP = next();
    public static final CRIcons I_COUPLING_BOTH = newRow(), I_COUPLING_COUPLE = next(), I_COUPLING_DECOUPLE = next();
    public static final CRIcons I_DOOR_MANUAL = newRow(), I_DOOR_NORMAL = next(), I_DOOR_SPECIAL = next(), I_DOOR_SPECIAL_INVERTED = next();
    public static final CRIcons I_SWITCH_MANUAL = newRow(), I_SWITCH_AUTO = next();
    public static final CRIcons I_SWAP_TRACKS = newRow();
    public static final CRIcons I_NARROW = newRow(), I_STANDARD = next(), I_WIDE = next(), I_FAVORITE = next(), I_FAVORITED = next();

    public CRIcons(int x, int y) {
        super(x, y);
        this.crIconX = x * 16;
        this.crIconY = y * 16;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_ATLAS, x, y, crIconX, crIconY, 16, 16, 256, 256);
    }

    private static CRIcons next() {
        return new CRIcons(++x, y);
    }

    private static CRIcons newRow() {
        return new CRIcons(x = 0, ++y);
    }
}
