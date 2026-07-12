package com.railwayteam.railways.multiloader.fabric;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.zurrtum.create.Create;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

public class PlatformAbstractionHelperImpl {
    public static int getBurnTime(Item item) {
        if (Create.SERVER != null) {
            return Create.SERVER.fuelValues().burnDuration(new ItemStack(item));
        }
        // The remote client has no server-side FuelValues table. Preserve the
        // vanilla liquid-fuel case and let the authoritative server validate modded fuels.
        return item == Items.LAVA_BUCKET ? 20_000 : 0;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> ArgumentType<T> enumArgument(Class<T> enumClass) {
        return (ArgumentType) StringArgumentType.word();
    }
}
