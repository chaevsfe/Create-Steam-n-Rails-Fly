package com.railwayteam.railways.multiloader.fabric;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.zurrtum.create.Create;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import java.util.Optional;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;

public class PlatformAbstractionHelperImpl {
    public static int getBurnTime(Item item) {
        if (Create.SERVER != null) {
            LootContext context = new LootContext.Builder(new LootParams.Builder(Create.SERVER.overworld()).create(LootContextParamSets.EMPTY)).create(Optional.empty());
            return ResolvableInt.getFromItem(new ItemStack(item), DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
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
