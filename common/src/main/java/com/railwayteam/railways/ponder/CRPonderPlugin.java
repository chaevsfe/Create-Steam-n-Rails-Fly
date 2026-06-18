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

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRPonderTags;
import com.zurrtum.create.client.ponder.api.registration.PonderPlugin;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class CRPonderPlugin implements PonderPlugin {
    public @NotNull String getModId() {
        return Railways.MOD_ID;
    }
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<Identifier> helper) {
        CRPonderIndex.register(helper);
    }
    public void registerTags(PonderTagRegistrationHelper<Identifier> helper) {
        CRPonderTags.register(helper);
    }
}
