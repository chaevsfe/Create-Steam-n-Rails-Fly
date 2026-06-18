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

import com.railwayteam.railways.content.custom_bogeys.renderer.unified.ElementProvider;
import com.railwayteam.railways.content.custom_bogeys.renderer.unified.ScrollHandle;
import com.zurrtum.create.client.content.processing.burner.ScrollTransformedInstance;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.client.foundation.render.SpecialModels;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

class VisualElementProvider implements ElementProvider<TransformedInstance> {
    @SuppressWarnings("unchecked")
    private static final Affine<TransformedInstance>[] EMPTY = new Affine[0];

    private VisualizationContext ctx;
    private final List<TransformedInstance> instances;

    VisualElementProvider(VisualizationContext ctx, List<TransformedInstance> instances) {
        this.ctx = ctx;
        this.instances = instances;
    }
    public @NotNull Affine<TransformedInstance> create(@NotNull PartialModel model) {
        if (ctx == null) {
            throw new IllegalStateException("Cannot create elements after build");
        }

        TransformedInstance instance = ctx.instancerProvider()
            .instancer(InstanceTypes.TRANSFORMED, SpecialModels.smoothLit(model))
            .createInstance();
        instances.add(instance);
        return instance;
    }
    public @NotNull Affine<TransformedInstance> @NotNull [] create(@NotNull PartialModel model, int count) {
        if (ctx == null) {
            throw new IllegalStateException("Cannot create elements after build");
        }

        if (count == 0) {
            return EMPTY;
        }

        TransformedInstance[] result = new TransformedInstance[count];
        ctx.instancerProvider()
            .instancer(InstanceTypes.TRANSFORMED, SpecialModels.smoothLit(model))
            .createInstances(result);
        instances.addAll(Arrays.asList(result));
        return result;
    }
    public @NotNull Pair<Affine<TransformedInstance>, ScrollHandle> createScrolling(@NotNull PartialModel model, @NotNull SpriteShiftEntry shift) {
        if (ctx == null) {
            throw new IllegalStateException("Cannot create elements after build");
        }

        ScrollTransformedInstance result = ctx.instancerProvider()
            .instancer(AllInstanceTypes.SCROLLING_TRANSFORMED, SpecialModels.smoothLit(model))
            .createInstance()
            .setSpriteShift(shift);
        instances.add(result);

        return Pair.of(result, new VisualScrollHandle(result));
    }
    public void freeze() {
        ctx = null;
    }

    private record VisualScrollHandle(ScrollTransformedInstance instance) implements ScrollHandle {
        public void scroll(float shiftV) {
            instance.offset(0, shiftV);
        }
    }
}
