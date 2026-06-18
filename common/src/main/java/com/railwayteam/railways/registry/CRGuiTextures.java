package com.railwayteam.railways.registry;

import com.railwayteam.railways.content.switches.TrackSwitchBlock;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;

public enum CRGuiTextures implements ScreenElement {
    TRAIN_HUD_SWITCH_BRASS,
    TRAIN_HUD_SWITCH_ANDESITE,
    TRAIN_HUD_SWITCH_LEFT,
    TRAIN_HUD_SWITCH_STRAIGHT,
    TRAIN_HUD_SWITCH_RIGHT,
    TRAIN_HUD_SWITCH_LEFT_WRONG,
    TRAIN_HUD_SWITCH_STRAIGHT_WRONG,
    TRAIN_HUD_SWITCH_RIGHT_WRONG,
    TRAIN_HUD_SWITCH_LOCKED,
    BOGEY_MENU,
    BOGEY_MENU_SCROLL_BAR,
    BOGEY_MENU_SCROLL_BAR_DISABLED;

    public static final int FONT_COLOR = 0x575F7A;
    public final int width = 0;
    public final int height = 0;

    public static CRGuiTextures getForSwitch(TrackSwitchBlock.SwitchState switchState, boolean isWrong) {
        return switch (switchState) {
            case REVERSE_LEFT -> isWrong ? TRAIN_HUD_SWITCH_LEFT_WRONG : TRAIN_HUD_SWITCH_LEFT;
            case REVERSE_RIGHT -> isWrong ? TRAIN_HUD_SWITCH_RIGHT_WRONG : TRAIN_HUD_SWITCH_RIGHT;
            case NORMAL -> isWrong ? TRAIN_HUD_SWITCH_STRAIGHT_WRONG : TRAIN_HUD_SWITCH_STRAIGHT;
        };
    }

    public void bind() {
    }

    public void render(GuiGraphics graphics, int x, int y) {
    }

    public void render(GuiGraphics graphics, int x, int y, int textureWidth, int textureHeight) {
    }

    public void render(GuiGraphics graphics, int x, int y, Color c) {
    }
}
