package com.railwayteam.railways.registry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.railwayteam.railways.Railways;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

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
    public void render(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_ATLAS, x, y, crIconX, crIconY, 16, 16, 256, 256);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int x, int y, int color) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICON_ATLAS, x, y, crIconX, crIconY, 16, 16, 16, 16, 256, 256, color);
    }

    @Override
    public RenderType bind() {
        return RenderTypes.text(ICON_ATLAS);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int color) {
        VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.text(ICON_ATLAS));
        Matrix4f matrix = poseStack.last().pose();
        Color rgb = new Color(color);
        int light = 15728880;
        float u0 = crIconX / 256f;
        float u1 = (crIconX + 16) / 256f;
        float v0 = crIconY / 256f;
        float v1 = (crIconY + 16) / 256f;
        vertex(consumer, matrix, 0, 0, rgb, u0, v0, light);
        vertex(consumer, matrix, 0, 1, rgb, u0, v1, light);
        vertex(consumer, matrix, 1, 1, rgb, u1, v1, light);
        vertex(consumer, matrix, 1, 0, rgb, u1, v0, light);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, Color color, float u, float v, int light) {
        consumer.addVertex(matrix, x, y, 0)
            .setColor(color.getRed(), color.getGreen(), color.getBlue(), 255)
            .setUv(u, v)
            .setLight(light);
    }

    private static CRIcons next() {
        return new CRIcons(++x, y);
    }

    private static CRIcons newRow() {
        return new CRIcons(x = 0, ++y);
    }
}
