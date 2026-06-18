package com.railwayteam.railways.content.cycle_menu;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.railwayteam.railways.content.bogey_menu.handler.BogeyMenuEventsHandler;
import com.railwayteam.railways.registry.CRKeys;
import com.railwayteam.railways.util.client.ClientUtils;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RadialTagCycleMenu extends AbstractSimiScreen {
    private int ticksOpen;
    private int hoveredSlot = -1;
    private boolean scrollMode;
    private int scrollSlot = 0;
    private final TagKey<Item> tag;
    private final List<Item> cycle;
    private final @Nullable ItemStack templateStack;
    private final int slotCount;

    RadialTagCycleMenu(TagKey<Item> tag, List<Item> cycle, @Nullable ItemStack templateStack) {
        super(Component.empty());
        this.tag = tag;
        this.cycle = cycle;
        this.templateStack = templateStack;
        this.slotCount = Math.max(8, cycle.size());
    }

    @Override
    protected void renderWindowBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int alpha = ((int) (0x50 * Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f))) << 24;
        graphics.fillGradient(0, 0, width, height, 0x101010 | alpha, 0x101010 | alpha);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        float fade = Mth.clamp((ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f, 1 / 512f, 1);
        Matrix3x2fStack pose = graphics.pose();

        hoveredSlot = -1;
        Window window = Minecraft.getInstance().getWindow();
        float hoveredX = mouseX - window.getGuiScaledWidth() / 2f;
        float hoveredY = mouseY - window.getGuiScaledHeight() / 2f;

        double interSlotAngle = 360.0 / slotCount;
        float distance = hoveredX * hoveredX + hoveredY * hoveredY;
        if (distance > 25 && distance < 10000) {
            double offset = 90 - 1.5 * interSlotAngle;
            hoveredSlot = (int) Math.floor((Mth.floor((Math.toDegrees(Math.atan2(hoveredY, hoveredX)) + 360 + 180 - offset)) % 360) / interSlotAngle);
        }
        if (scrollMode && distance > 150)
            scrollMode = false;

        pose.pushMatrix();
        pose.translate(width / 2f, height / 2f);

        Component tip = null;
        Identifier tagLoc = tag.location();
        Component title = Component.translatable("tag.item." + tagLoc.getNamespace() + "." + tagLoc.getPath().replace('/', '.'));

        for (int slot = 0; slot < slotCount; slot++) {
            pose.pushMatrix();
            double radius = -(5 * slotCount) + (10 * (1 - fade) * (1 - fade));
            double angle = slot * interSlotAngle - interSlotAngle;
            pose.rotate((float) Math.toRadians(angle));
            pose.translate(0, (float) radius);
            pose.rotate((float) Math.toRadians(-angle));
            pose.translate(-12, -12);

            boolean selected = slot == (scrollMode ? scrollSlot : hoveredSlot);
            if (slot < cycle.size()) {
                ItemStack stack = createDisplayStack(cycle.get(slot));
                AllGuiTextures.TOOLBELT_SLOT.render(graphics, 0, 0);
                GuiGameElement.of(stack)
                    .at(3, 3)
                    .render(graphics);

                if (selected) {
                    AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -1, -1);
                    tip = Component.empty().append(stack.getHoverName()).withStyle(ChatFormatting.GOLD);
                }
            } else {
                AllGuiTextures.TOOLBELT_EMPTY_SLOT.render(graphics, 0, 0);
            }
            pose.popMatrix();
        }

        int alpha = Math.min(255, (int) (fade * 255.0F));
        if (alpha > 8) {
            pose.pushMatrix();
            pose.translate(0, -80);
            drawCentered(graphics, title, alpha);
        }

        pose.popMatrix();

        if (tip != null && alpha > 8) {
            pose.pushMatrix();
            pose.translate(width / 2f, height - 68f);
            drawCentered(graphics, tip, alpha);
        }
    }

    private ItemStack createDisplayStack(Item item) {
        return templateStack == null ? new ItemStack(item) : templateStack.transmuteCopy(item, 1);
    }

    private void drawCentered(GuiGraphics graphics, Component title, int alpha) {
        Matrix3x2fStack pose = graphics.pose();
        int color = 0xFFFFFF | (alpha << 24);
        graphics.drawString(font, title, -font.width(title) / 2, -4, color);
        pose.popMatrix();
    }

    @Override
    public void tick() {
        ticksOpen++;
        super.tick();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int selected = scrollMode ? scrollSlot : hoveredSlot;
        if (event.button() == 0 && selected >= 0 && selected < cycle.size()) {
            TagCycleHandlerClient.select(cycle.get(selected));
            onClose();
            BogeyMenuEventsHandler.COOLDOWN = 2;
            TagCycleHandlerClient.COOLDOWN = 2;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        Window window = Minecraft.getInstance().getWindow();
        double hoveredX = mouseX - window.getGuiScaledWidth() / 2f;
        double hoveredY = mouseY - window.getGuiScaledHeight() / 2f;
        double distance = hoveredX * hoveredX + hoveredY * hoveredY;
        if (distance <= 150) {
            scrollMode = true;
            scrollSlot = (((int) (scrollSlot - scrollY)) + slotCount) % slotCount;
            for (int i = 0; i < 10; i++) {
                if (scrollSlot < cycle.size())
                    break;
                scrollSlot -= Mth.sign(scrollY);
                scrollSlot = (scrollSlot + slotCount) % slotCount;
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        InputConstants.Key mouseKey = InputConstants.getKey(event);
        if (ClientUtils.isActiveAndMatches(CRKeys.CYCLE_MENU.getKeybind(), mouseKey)) {
            onClose();
            return true;
        }
        return super.keyReleased(event);
    }
}
