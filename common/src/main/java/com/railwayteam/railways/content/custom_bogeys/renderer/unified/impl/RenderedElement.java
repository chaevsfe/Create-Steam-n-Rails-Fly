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

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.custom_bogeys.renderer.unified.ScrollHandle;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;

class RenderedElement implements Affine<RenderedElement> {
    final Matrix4f pose = new Matrix4f();
    public RenderedElement translate(float x, float y, float z) {
        pose.translate(x, y, z);
        return this;
    }
    public RenderedElement rotate(Quaternionfc quaternion) {
        pose.rotate(quaternion);
        return this;
    }
    public RenderedElement scale(float factorX, float factorY, float factorZ) {
        pose.scale(factorX, factorY, factorZ);
        return this;
    }

    public RenderedElement setTransform(Matrix4f matrix) {
        pose.set(matrix);
        return this;
    }

    public RenderedElement setTransform(PoseStack.Pose pose) {
        this.pose.set(pose.pose());
        return this;
    }

    public RenderedElement setTransform(PoseStack stack) {
        return setTransform(stack.last());
    }
    public RenderedElement rotateAround(Quaternionfc quaternion, float x, float y, float z) {
        pose.rotateAround(quaternion, x, y, z);
        return this;
    }
    public RenderedElement rotateCentered(float radians, float axisX, float axisY, float axisZ) {
        pose.translate(Affine.CENTER, Affine.CENTER, Affine.CENTER)
            .rotate(radians, axisX, axisY, axisZ)
            .translate(-Affine.CENTER, -Affine.CENTER, -Affine.CENTER);
        return this;
    }
    public RenderedElement rotateXCentered(float radians) {
        pose.translate(Affine.CENTER, Affine.CENTER, Affine.CENTER)
            .rotateX(radians)
            .translate(-Affine.CENTER, -Affine.CENTER, -Affine.CENTER);
        return this;
    }
    public RenderedElement rotateYCentered(float radians) {
        pose.translate(Affine.CENTER, Affine.CENTER, Affine.CENTER)
            .rotateY(radians)
            .translate(-Affine.CENTER, -Affine.CENTER, -Affine.CENTER);
        return this;
    }
    public RenderedElement rotateZCentered(float radians) {
        pose.translate(Affine.CENTER, Affine.CENTER, Affine.CENTER)
            .rotateZ(radians)
            .translate(-Affine.CENTER, -Affine.CENTER, -Affine.CENTER);
        return this;
    }
    public RenderedElement rotate(float radians, float axisX, float axisY, float axisZ) {
        pose.rotate(radians, axisX, axisY, axisZ);
        return this;
    }
    public RenderedElement rotate(AxisAngle4f axisAngle) {
        pose.rotate(axisAngle);
        return this;
    }
    public RenderedElement rotateX(float radians) {
        pose.rotateX(radians);
        return this;
    }
    public RenderedElement rotateY(float radians) {
        pose.rotateY(radians);
        return this;
    }
    public RenderedElement rotateZ(float radians) {
        pose.rotateZ(radians);
        return this;
    }

    record Single(RenderedElement element, PartialModel model) {}
    record Multiple(RenderedElement[] elements, PartialModel model) {}

    static class Scrolling implements ScrollHandle {
        final RenderedElement element;
        final PartialModel model;
        final SpriteShiftEntry entry;
        float shiftV;

        Scrolling(RenderedElement element, PartialModel model, SpriteShiftEntry entry) {
            this.element = element;
            this.model = model;
            this.entry = entry;
        }
        public void scroll(float shiftV) {
            this.shiftV = shiftV;
        }
    }
}
