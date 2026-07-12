package com.railwayteam.railways.content.roller_extensions.fabric;

import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.logistics.crate.CreativeCrateMountedStorage;
import com.zurrtum.create.infrastructure.items.CombinedInvWrapper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

public class TrackReplacePaverImpl {
    public static ItemStack extract(FilterItemStack filter, MovementContext context, int amt) {
        return extractFromStorage(
            filter,
            context.world,
            context.contraption.getStorage().getAllItems(),
            hasCreativeSupply(filter, context),
            amt
        );
    }

    public static void refund(ItemStack stack, FilterItemStack filter, MovementContext context) {
        if (stack.isEmpty() || hasCreativeSupply(filter, context)) {
            return;
        }
        refundToStorage(context.contraption.getStorage().getAllItems(), stack);
    }

    private static boolean hasCreativeSupply(FilterItemStack filter, MovementContext context) {
        if (context.contraption.hasUniversalCreativeCrate) {
            return true;
        }
        return context.contraption.getStorage().getAllItemStorages().values().stream()
            .filter(CreativeCrateMountedStorage.class::isInstance)
            .map(CreativeCrateMountedStorage.class::cast)
            .map(storage -> storage.getItem(0))
            .anyMatch(stack -> !stack.isEmpty() && filter.test(context.world, stack));
    }

    public static ItemStack extractFromStorage(FilterItemStack filter, Level level, CombinedInvWrapper inventory,
                                               boolean creative, int amount) {
        if (amount <= 0) {
            return ItemStack.EMPTY;
        }

        Predicate<ItemStack> predicate = stack -> filter.test(level, stack);
        if (creative) {
            ItemStack supplied = filter.item();
            if (supplied.isEmpty() || filter.isFilterItem()) {
                supplied = inventory.count(predicate, 1);
            }
            if (supplied.isEmpty() || !predicate.test(supplied)) {
                return ItemStack.EMPTY;
            }
            return supplied.copyWithCount(amount);
        }

        // Select one concrete stack variant that can satisfy the whole request. This prevents
        // an attribute/list filter from consuming several incompatible matching variants.
        ItemStack candidate = inventory.preciseCount(predicate, amount);
        if (candidate.isEmpty() || candidate.getCount() != amount) {
            return ItemStack.EMPTY;
        }

        ItemStack extracted = inventory.extract(
            stack -> ItemStack.isSameItemSameComponents(candidate, stack),
            amount
        );
        if (extracted.getCount() == amount) {
            return extracted;
        }

        // A custom or externally attached inventory may change between preflight and extraction.
        // Never allow a partial payment to replace a full curve: put it back atomically or fail loud.
        if (!extracted.isEmpty()) {
            refundToStorage(inventory, extracted);
        }
        return ItemStack.EMPTY;
    }

    static void refundToStorage(CombinedInvWrapper inventory, ItemStack stack) {
        int amount = stack.getCount();
        int inserted = inventory.insert(stack, amount);
        if (inserted != amount) {
            throw new IllegalStateException(
                "Could not roll back Railway roller track extraction: restored " + inserted + " of " + amount
            );
        }
    }
}
