/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.switches;

import com.railwayteam.railways.registry.CRGuiTextures;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public class TrainHUDSwitchExtension {

    public static @Nullable TrackSwitchBlock.SwitchState switchState;
    public static boolean isAutomaticSwitch = false;
    public static boolean isWrong = false;
    public static boolean isLocked = false;
    static LerpedFloat switchProgress = LerpedFloat.linear();

    public static void clearSwitchState() {
        switchState = null;
    }

    public static void tickAnimation() {
        switchProgress.chase(switchState != null ? 1.0 : 0.0, .5, LerpedFloat.Chaser.EXP);
        switchProgress.tickChaser();
    }

    public static void renderOverlay(GuiGraphics graphics, float partialTicks, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return;

        float progress = switchProgress.getValue(partialTicks);
        if (progress <= 0)
            return;

        int baseX = width / 2 - 91;
        int baseY = height - 29;

        CRGuiTextures bg = isAutomaticSwitch ?
                CRGuiTextures.TRAIN_HUD_SWITCH_BRASS :
                CRGuiTextures.TRAIN_HUD_SWITCH_ANDESITE;
        int renderedHeight = (int) (bg.height * progress + 0.5);
        int yPos = baseY + (int) (-16 * progress - 0.5);
        graphics.blit(RenderPipelines.GUI_TEXTURED, bg.location, baseX + 131, yPos,
                bg.startX, bg.startY, bg.width, renderedHeight, 256, 256);

        if (progress > 0.99 && switchState != null) {
            switch (switchState) {
                case NORMAL -> CRGuiTextures.getForSwitch(switchState, isWrong).render(graphics, baseX + 152, baseY - 13);
                case REVERSE_LEFT -> CRGuiTextures.getForSwitch(switchState, isWrong).render(graphics, baseX + 142, baseY - 13);
                case REVERSE_RIGHT -> CRGuiTextures.getForSwitch(switchState, isWrong).render(graphics, baseX + 162, baseY - 13);
            }
            if (isLocked)
                CRGuiTextures.TRAIN_HUD_SWITCH_LOCKED.render(graphics, baseX + 134, baseY - 13);
        }
    }
}
