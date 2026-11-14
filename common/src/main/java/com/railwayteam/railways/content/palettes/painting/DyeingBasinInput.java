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

package com.railwayteam.railways.content.palettes.painting;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.CRFluids;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.basin.BasinInput;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DyeingBasinInput {
    private DyeingBasinInput() {}

    public static BasinInput mask(MixingRecipe recipe, BasinInput input) {
        Item result = paintedResult(recipe);
        if (result == null) return input;
        return new BasinInput(
            input.filter(),
            input.heat(),
            input.fluids(),
            new HidingContainer(input.items(), result),
            input.random(),
            input.blockEntity()
        );
    }

    private static @Nullable Item paintedResult(MixingRecipe recipe) {
        List<ProcessingOutput> results = recipe.results();
        if (results.isEmpty()) return null;

        Item item = results.getFirst().item().value();
        if (!Railways.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace())) return null;

        Fluid paint = CRFluids.PAINT.get();
        for (FluidIngredient ingredient : recipe.fluidIngredients()) {
            for (Fluid fluid : ingredient.getMatchingFluids()) {
                if (paint.isSame(fluid)) return item;
            }
        }
        return null;
    }

    private record HidingContainer(Container items, Item hidden) implements Container {
        private boolean hides(int slot) {
            return items.getItem(slot).is(hidden);
        }

        @Override
        public int getContainerSize() {
            return items.getContainerSize();
        }

        @Override
        public boolean isEmpty() {
            for (int slot = 0, size = getContainerSize(); slot < size; slot++) {
                if (!getItem(slot).isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            ItemStack stack = items.getItem(slot);
            return stack.is(hidden) ? ItemStack.EMPTY : stack;
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            return hides(slot) ? ItemStack.EMPTY : items.removeItem(slot, count);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return hides(slot) ? ItemStack.EMPTY : items.removeItemNoUpdate(slot);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (!hides(slot)) items.setItem(slot, stack);
        }

        @Override
        public int getMaxStackSize() {
            return items.getMaxStackSize();
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return items.getMaxStackSize(stack);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return !hides(slot) && items.canPlaceItem(slot, stack);
        }

        @Override
        public void setChanged() {
            items.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return items.stillValid(player);
        }

        @Override
        public void clearContent() {
            for (int slot = 0, size = getContainerSize(); slot < size; slot++) {
                setItem(slot, ItemStack.EMPTY);
            }
        }
    }
}
