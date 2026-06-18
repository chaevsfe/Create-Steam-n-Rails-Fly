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

package com.railwayteam.railways.content.custom_bogeys.special.invisible;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.content.trains.bogey.BogeyVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class InvisibleBogeyVisual implements BogeyVisual {
    public InvisibleBogeyVisual(VisualizationContext ctx, float partialTick, boolean inContraption) {}
    public void update(CompoundTag bogeyData, float wheelAngle, PoseStack poseStack) {}
    public void hide() {}
    public void updateLight(int packedLight) {}
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {}
    public void delete() {}
}
