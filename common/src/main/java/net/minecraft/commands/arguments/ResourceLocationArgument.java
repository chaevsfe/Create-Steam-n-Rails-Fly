package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.resources.Identifier;

public class ResourceLocationArgument {
    public static ArgumentType<String> id() {
        return StringArgumentType.word();
    }

    public static Identifier getId(CommandContext<?> context, String name) {
        return Identifier.parse(StringArgumentType.getString(context, name));
    }
}
