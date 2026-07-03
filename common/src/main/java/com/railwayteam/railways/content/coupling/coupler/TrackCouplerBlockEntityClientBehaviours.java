/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

package com.railwayteam.railways.content.coupling.coupler;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

/**
 * ScrollValueBehaviour (client) requires a ValueBoxTransform for rendering/interaction, a
 * client-only Create Fly type. Kept in its own class so TrackCouplerBlockEntity never embeds
 * that type in one of its own method descriptors - a class's own methods get fully verified
 * (and their referenced types resolved) the moment the class loads, regardless of any runtime
 * Env guard around the call site that would construct one.
 */
@Environment(EnvType.CLIENT)
public class TrackCouplerBlockEntityClientBehaviours {
    public static BlockEntityBehaviour<?> createEdgeSpacingScroll(TrackCouplerBlockEntity be) {
        return new ScrollValueBehaviour<TrackCouplerBlockEntity, ServerScrollValueBehaviour>(
            Component.translatable("railways.coupler.edge_spacing"), be, new TrackCouplerBlockEntity.TrackCouplerValueBoxTransform(true)
        ) {{
            needsWrench = true;
        }}
            .withFormatter(i -> String.valueOf(Component.translatable("railways.coupler.edge_spacing.meters")))
            .withFormatter(i -> i + "m");
    }
}
