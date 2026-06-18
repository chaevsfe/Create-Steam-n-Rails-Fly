package com.railwayteam.railways.content.switches;

import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.Nullable;

public class TrainHUDSwitchExtension {
    public static @Nullable TrackSwitchBlock.SwitchState switchState;
    public static boolean isAutomaticSwitch = false;
    public static boolean isWrong = false;
    public static boolean isLocked = false;

    public static void tick() {
    }

    public static void renderOverlay(GuiGraphics graphics, float partialTicks, int width, int height) {
    }
}
