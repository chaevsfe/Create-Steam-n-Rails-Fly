/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.util.ItemUtils;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PotatoCannonItem.class)
public class MixinPotatoCannonItem {
    @WrapOperation(
        method = "use",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/equipment/potatoCannon/PotatoCannonItem;getAmmo(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lcom/zurrtum/create/content/equipment/potatoCannon/PotatoCannonItem$Ammo;"
        )
    )
    private static @Nullable PotatoCannonItem.Ammo railways$splitPitcher(
        Player player,
        ItemStack heldStack,
        Operation<PotatoCannonItem.Ammo> original
    ) {
        PotatoCannonItem.Ammo ammo = original.call(player, heldStack);
        if (ammo == null || !(ammo.stack().getItem() instanceof PaintPitcherItem item)) return ammo;

        ItemStack source = ammo.stack();
        PaintPitcherItem.CannonShot shot = item.splitForCannon(source);
        if (shot.usedLevels() == 0) {
            replaceInventoryReference(player, source, shot.remainder());
            return null;
        }

        if (!player.isCreative() && !ItemUtils.isUnbreakable(source)) {
            replaceInventoryReference(player, source, shot.remainder());
        }

        return new PotatoCannonItem.Ammo(shot.projectile(), ammo.type());
    }

    private static void replaceInventoryReference(Player player, ItemStack source, ItemStack replacement) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot) == source) {
                inventory.setItem(slot, replacement);
                inventory.setChanged();
                return;
            }
        }

        // Projectile providers are normally inventory-backed. If another mod supplies a detached
        // stack, consume that stack and return the remainder to the inventory instead of allowing
        // an infinite paint source.
        source.setCount(0);
        inventory.placeItemBackInInventory(replacement);
    }
}
