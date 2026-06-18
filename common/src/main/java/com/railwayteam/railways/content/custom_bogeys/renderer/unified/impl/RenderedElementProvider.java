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
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class RenderedElementProvider implements ElementProvider<RenderedElement> {
    private final List<RenderedElement.Single> singleElements;
    private final List<RenderedElement.Multiple> multipleElements;
    private final List<RenderedElement.Scrolling> scrollingElements;
    private boolean frozen = false;

    RenderedElementProvider(List<RenderedElement.Single> singleElements, List<RenderedElement.Multiple> multipleElements, List<RenderedElement.Scrolling> scrollingElements) {
        this.singleElements = singleElements;
        this.multipleElements = multipleElements;
        this.scrollingElements = scrollingElements;
    }
    public @NotNull Affine<RenderedElement> create(@NotNull PartialModel model) {
        if (frozen) {
            throw new IllegalStateException("Cannot create elements after build");
        }

        RenderedElement element = new RenderedElement();
        singleElements.add(new RenderedElement.Single(element, model));
        return element;
    }
    public @NotNull Affine<RenderedElement> @NotNull [] create(@NotNull PartialModel model, int count) {
        if (frozen) {
            throw new IllegalStateException("Cannot create elements after build");
        }

        RenderedElement[] elements = new RenderedElement[count];
        for (int i = 0; i < count; i++) {
            elements[i] = new RenderedElement();
        }
        multipleElements.add(new RenderedElement.Multiple(elements, model));
        return elements;
    }
    public @NotNull Pair<Affine<RenderedElement>, ScrollHandle> createScrolling(@NotNull PartialModel model, @NotNull SpriteShiftEntry shift) {
        RenderedElement element = new RenderedElement();
        RenderedElement.Scrolling scrolling = new RenderedElement.Scrolling(element, model, shift);
        scrollingElements.add(scrolling);
        return Pair.of(element, scrolling);
    }
    public void freeze() {
        frozen = true;
    }
}
