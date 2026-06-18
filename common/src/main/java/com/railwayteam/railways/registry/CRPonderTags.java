/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.registry;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public class CRPonderTags {
    public static void register(PonderTagRegistrationHelper<Identifier> helper) {
        PonderTagRegistrationHelper<Item> HELPER = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);

        HELPER.addToTag(AllCreatePonderTags.DISPLAY_SOURCES)
            .add(AllBlocks.TRACK_SIGNAL.asItem())
            .add(CRBlocks.TRACK_COUPLER.asItem())
            .add(CRBlocks.ANDESITE_SWITCH.asItem())
            .add(CRBlocks.BRASS_SWITCH.asItem());
        HELPER.addToTag(AllCreatePonderTags.DISPLAY_TARGETS)
            .add(CRBlocks.SEMAPHORE.asItem());
    }
}
