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

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolboxContainer;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolboxScreen;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.client.AllMenuScreens;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;

public class CRContainerTypes {
    public static final MenuType<ToolboxBlockEntity> MOUNTED_TOOLBOX = Registry.register(
        CreateRegistries.MENU_TYPE,
        Railways.asResource("mounted_toolbox"),
        MountedToolboxContainer::new
    );

    @SuppressWarnings("EmptyMethod")
    public static void register() {
    }

    @Environment(EnvType.CLIENT)
    public static void registerScreens() {
        AllMenuScreens.register(MOUNTED_TOOLBOX, MountedToolboxScreen::create);
    }
}
