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

package com.railwayteam.railways.content.item;

import com.railwayteam.railways.Railways;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.foundation.item.ItemDescription;
import com.zurrtum.create.client.foundation.item.KineticStats;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Kept in its own class (rather than inline in {@link Railways}) so the client-only
 * {@code TooltipModifier} return type never appears in a method descriptor belonging to a
 * class loaded on the dedicated server. A synthetic lambda body with that descriptor gets
 * verified - and its return type resolved - the moment its declaring class is loaded,
 * regardless of any runtime {@code Env} guard around the call site.
 */
@Environment(EnvType.CLIENT)
public class RailwaysTooltipModifiers {
  public static void register() {
    Railways.registrate().setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
        .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
  }
}
