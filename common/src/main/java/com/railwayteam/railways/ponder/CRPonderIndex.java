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

package com.railwayteam.railways.ponder;

import com.railwayteam.railways.ponder.scenes.ConductorScenes;
import com.railwayteam.railways.ponder.scenes.DoorScenes;
import com.railwayteam.railways.ponder.scenes.TrainScenes;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRItems;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;


public class CRPonderIndex {
    public static void register(PonderSceneRegistrationHelper<Identifier> helper) {
        PonderSceneRegistrationHelper<Item> HELPER = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);

        HELPER.forComponents(CRBlocks.SEMAPHORE.asItem())
                .addStoryBoard("semaphore", TrainScenes::signaling);

        HELPER.forComponents(CRBlocks.TRACK_COUPLER.asItem())
                .addStoryBoard("coupler", TrainScenes::coupling);

        Item[] conductorCapItems = CRItems.ITEM_CONDUCTOR_CAP.values().stream()
                .map(e -> e.asItem()).toArray(Item[]::new);
        HELPER.forComponents(conductorCapItems)
            .addStoryBoard("conductor", ConductorScenes::constructing)
            .addStoryBoard("conductor_redstone", ConductorScenes::redstoning)
            .addStoryBoard("conductor", ConductorScenes::toolboxing);

        HELPER.forComponents(
            AllBlocks.ANDESITE_DOOR.asItem(),
            AllBlocks.BRASS_DOOR.asItem(),
            AllBlocks.COPPER_DOOR.asItem(),
            AllBlocks.TRAIN_DOOR.asItem(),
            AllBlocks.FRAMED_GLASS_DOOR.asItem()
        )
                .addStoryBoard("door_modes", DoorScenes::modes);

        HELPER.forComponents(CRBlocks.ANDESITE_SWITCH.asItem(), CRBlocks.BRASS_SWITCH.asItem())
                .addStoryBoard("switch", TrainScenes::trackSwitch);
    }
}
