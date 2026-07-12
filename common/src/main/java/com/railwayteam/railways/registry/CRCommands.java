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
