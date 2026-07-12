package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.switches.TrackSwitchBlock;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public enum CRGuiTextures implements ScreenElement {
    TRAIN_HUD_SWITCH_BRASS("widgets", 0, 0, 42, 16),
    TRAIN_HUD_SWITCH_ANDESITE("widgets", 0, 16, 42, 16),
    TRAIN_HUD_SWITCH_LEFT("widgets", 1, 33, 10, 10),
    TRAIN_HUD_SWITCH_STRAIGHT("widgets", 13, 33, 10, 10),
    TRAIN_HUD_SWITCH_RIGHT("widgets", 25, 33, 10, 10),
    TRAIN_HUD_SWITCH_LEFT_WRONG("widgets", 1, 45, 10, 10),
    TRAIN_HUD_SWITCH_STRAIGHT_WRONG("widgets", 13, 45, 10, 10),
    TRAIN_HUD_SWITCH_RIGHT_WRONG("widgets", 25, 45, 10, 10),
    TRAIN_HUD_SWITCH_LOCKED("widgets", 37, 45, 10, 10),
    BOGEY_MENU("bogeymenu", 279, 184),
    BOGEY_MENU_SCROLL_BAR("bogeymenu", 280, 0, 8, 15),
    BOGEY_MENU_SCROLL_BAR_DISABLED("bogeymenu", 288, 0, 8, 15);

    public static final int FONT_COLOR = 0x575F7A;
    public final Identifier location;
    public final int width;
    public final int height;
    public final int startX;
    public final int startY;

    CRGuiTextures(String location, int width, int height) {
        this(location, 0, 0, width, height);
    }

    CRGuiTextures(String location, int startX, int startY, int width, int height) {
        this.location = Railways.asResource("textures/gui/" + location + ".png");
        this.width = width;
        this.height = height;
        this.startX = startX;
        this.startY = startY;
    }

    public static CRGuiTextures getForSwitch(TrackSwitchBlock.SwitchState switchState, boolean isWrong) {
        return switch (switchState) {
            case REVERSE_LEFT -> isWrong ? TRAIN_HUD_SWITCH_LEFT_WRONG : TRAIN_HUD_SWITCH_LEFT;
            case REVERSE_RIGHT -> isWrong ? TRAIN_HUD_SWITCH_RIGHT_WRONG : TRAIN_HUD_SWITCH_RIGHT;
            case NORMAL -> isWrong ? TRAIN_HUD_SWITCH_STRAIGHT_WRONG : TRAIN_HUD_SWITCH_STRAIGHT;
        };
    }

    public void bind() {
    }

    public void render(GuiGraphicsExtractor graphics, int x, int y) {
        render(graphics, x, y, 256, 256);
    }

    public void render(GuiGraphicsExtractor graphics, int x, int y, int textureWidth, int textureHeight) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, location, x, y, startX, startY, width, height, textureWidth, textureHeight);
    }

    public void render(GuiGraphicsExtractor graphics, int x, int y, Color c) {
        render(graphics, x, y);
    }
}
