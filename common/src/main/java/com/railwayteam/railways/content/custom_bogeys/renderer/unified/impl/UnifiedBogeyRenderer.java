/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.content.custom_bogeys.renderer.unified.impl;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.railwayteam.railways.content.custom_bogeys.renderer.unified.BogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.unified.BogeyDisplayHolder;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import com.zurrtum.create.client.content.trains.bogey.BogeyRenderer;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

@ApiStatus.Internal
public class UnifiedBogeyRenderer implements BogeyRenderer, BogeyDisplayHolder {
    private final Couple<Renderer> renderers;
    private final @Nullable BogeyRenderer customRenderer;

    public UnifiedBogeyRenderer(BogeyDisplay.Factory factory) {
        this.renderers = Couple.createWithContext(inContraption -> Renderer.create(factory, inContraption));
        this.customRenderer = factory.createCustomRenderer();
    }
    public void runWithDisplay(Consumer<BogeyDisplay> consumer) {
        consumer.accept(renderers.getFirst().display);
        consumer.accept(renderers.getSecond().display);

        if (customRenderer instanceof BogeyDisplayHolder customDisplayHolder) {
            customDisplayHolder.runWithDisplay(consumer);
        }
    }
    @Override
    public BogeyRenderState getRenderData(@Nullable CompoundTag bogeyData, float wheelAngle, float partialTick,
                                          int packedLight, boolean inContraption) {
        final Renderer renderer = renderers.get(inContraption);
        if (bogeyData == null)
            bogeyData = new CompoundTag();
        renderer.reset();
        renderer.display.update(bogeyData, wheelAngle);
        BogeyRenderState customState = customRenderer == null ? null
            : customRenderer.getRenderData(bogeyData, wheelAngle, partialTick, packedLight, inContraption);
        List<Matrix4f> elementPoses = renderer.allElements.stream()
            .map(element -> new Matrix4f(element.pose))
            .toList();
        List<Float> scrollingOffsets = renderer.scrollingElements.stream()
            .map(element -> element.shiftV)
            .toList();
        return new RenderState(renderer, elementPoses, scrollingOffsets, customState, packedLight);
    }

    private record RenderState(Renderer renderer, List<Matrix4f> elementPoses, List<Float> scrollingOffsets,
                               @Nullable BogeyRenderState customState, int packedLight)
        implements BogeyRenderState, SubmitNodeCollector.CustomGeometryRenderer {

        @Override
        public void render(PoseStack poseStack, SubmitNodeCollector queue) {
            poseStack.translate(0, -1.5 - 1 / 128f, 0);
            queue.submitCustomGeometry(poseStack, RenderTypes.cutoutMovingBlock(), this);
            if (customState != null)
                customState.render(poseStack, queue);
        }

        @Override
        public void render(PoseStack.Pose matrices, VertexConsumer buffer) {
            PoseStack ms = new PoseStack();
            int poseIndex = 0;

            for (RenderedElement.Single element : renderer.singleElements) {
                setPose(ms.last(), matrices, elementPoses.get(poseIndex++));

                CachedBuffers.partial(element.model(), Blocks.AIR.defaultBlockState())
                    .light(packedLight)
                    .overlay(OverlayTexture.NO_OVERLAY)
                    .renderInto(ms.last(), buffer);
            }

            for (RenderedElement.Multiple elements : renderer.multipleElements) {
                SuperByteBuffer sbb = CachedBuffers.partial(elements.model(), Blocks.AIR.defaultBlockState());

                for (RenderedElement element : elements.elements()) {
                    setPose(ms.last(), matrices, elementPoses.get(poseIndex++));

                    sbb.light(packedLight)
                        .overlay(OverlayTexture.NO_OVERLAY)
                        .renderInto(ms.last(), buffer);
                }
            }

            for (int i = 0; i < renderer.scrollingElements.size(); i++) {
                RenderedElement.Scrolling element = renderer.scrollingElements.get(i);
                setPose(ms.last(), matrices, elementPoses.get(poseIndex++));
                float spriteSize = element.entry.getTarget().getV1() - element.entry.getTarget().getV0();
                float shiftV = scrollingOffsets.get(i);
                float scrollV = shiftV - Mth.floor(shiftV);
                scrollV = scrollV * spriteSize * 0.5f;
                CachedBuffers.partial(element.model, Blocks.AIR.defaultBlockState())
                    .light(packedLight)
                    .overlay(OverlayTexture.NO_OVERLAY)
                    .shiftUVScrolling(element.entry, scrollV)
                    .renderInto(ms.last(), buffer);
            }
        }

        private static void setPose(PoseStack.Pose target, PoseStack.Pose base, Matrix4f transform) {
            target.pose().set(base.pose()).mul(transform);
            target.normal().set(base.normal()).mul(new Matrix3f(transform));
        }
    }

    private record Renderer(
        BogeyDisplay display,
        List<RenderedElement.Single> singleElements,
        List<RenderedElement.Multiple> multipleElements,
        List<RenderedElement.Scrolling> scrollingElements,
        List<RenderedElement> allElements
    ) {
        private void reset() {
            allElements.forEach(element -> element.pose.identity());
            scrollingElements.forEach(element -> element.shiftV = 0);
        }

        private static Renderer create(BogeyDisplay.Factory factory, boolean inContraption) {
            ArrayList<RenderedElement.Single> singleElements = new ArrayList<>();
            ArrayList<RenderedElement.Multiple> multipleElements = new ArrayList<>();
            ArrayList<RenderedElement.Scrolling> scrollingElements = new ArrayList<>();

            var prov = new RenderedElementProvider(singleElements, multipleElements, scrollingElements);
            BogeyDisplay display = factory.create(prov, inContraption);
            prov.freeze();

            List<RenderedElement> allElements = new ArrayList<>();
            singleElements.forEach(s -> allElements.add(s.element()));
            multipleElements.forEach(m -> Collections.addAll(allElements, m.elements()));
            scrollingElements.forEach(s -> allElements.add(s.element));

            return new Renderer(display, singleElements, multipleElements, scrollingElements, allElements);
        }
    }
}
