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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.commands.ShadowRealmCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class CRCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated, CommandBuildContext context) {
        LiteralCommandNode<CommandSourceStack> railwaysRoot = dispatcher.register(literal(Railways.MOD_ID)
            .then(ShadowRealmCommand.register()));

        CommandNode<CommandSourceStack> existingShortcut = dispatcher.findNode(List.of("snr"));
        if (existingShortcut == null) {
            dispatcher.register(literal("snr").redirect(railwaysRoot));
        } else if (dispatcher.findNode(List.of("snr", "shadow_realm")) == null) {
            existingShortcut.addChild(ShadowRealmCommand.register().build());
        }
    }
}
