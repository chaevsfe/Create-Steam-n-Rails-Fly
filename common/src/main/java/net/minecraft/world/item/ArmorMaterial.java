package net.minecraft.world.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public interface ArmorMaterial {
    int getDurabilityForType(@NotNull ArmorItem.Type type);

    int getDefenseForType(@NotNull ArmorItem.Type type);

    int getEnchantmentValue();

    @NotNull SoundEvent getEquipSound();

    @NotNull Ingredient getRepairIngredient();

    @NotNull String getName();

    float getToughness();

    float getKnockbackResistance();
}
