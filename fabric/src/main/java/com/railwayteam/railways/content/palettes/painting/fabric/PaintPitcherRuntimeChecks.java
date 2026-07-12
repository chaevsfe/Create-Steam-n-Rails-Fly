/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.palettes.painting.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.registry.CRFluids;
import com.railwayteam.railways.registry.CRItems;
import com.railwayteam.railways.registry.CRPotatoProjectileTypes;
import com.railwayteam.railways.util.ItemUtils;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.CreateRegistryKeys;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Development-time runtime checks for the component and Transfer API parts of paint pitchers. */
public final class PaintPitcherRuntimeChecks {
    private static final String SMOKE_MARKER = "RailwaysPaintPitcherSmokeMarker";
    private static boolean registered;
    private static boolean checked;

    private PaintPitcherRuntimeChecks() {
    }

    public static void register() {
        if (registered || !Utils.isDevEnv()) return;
        registered = true;
        ServerLifecycleEvents.SERVER_STARTED.register(PaintPitcherRuntimeChecks::run);
    }

    private static void run(MinecraftServer server) {
        if (checked) return;
        checked = true;

        checkItemComponents();
        checkFluidTransfer();
        checkCannonSplitting();
        checkPotatoProjectileRegistration(server);
        Railways.LOGGER.info(
            "Paint pitcher component, fluid-transfer, cannon-conservation, and potato-projectile runtime checks passed"
        );
    }

    private static void checkItemComponents() {
        PaintPitcherItem pitcher = CRItems.PAINT_PITCHERS.get(PalettesColor.RED).get();
        ItemStack full = new ItemStack(pitcher);
        require(pitcher.getLevels(full) == PaintPitcherItem.MAX_LEVELS, "new pitcher is not full");

        full.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        full.set(DataComponents.CUSTOM_NAME, Component.literal("component-copy-check"));
        CustomData.update(DataComponents.CUSTOM_DATA, full, tag -> tag.putBoolean(SMOKE_MARKER, true));

        ItemStack partial = pitcher.copyAsFilledStack(full, 7);
        require(pitcher.getLevels(partial) == 7, "partial fill level did not persist");
        require(ItemUtils.isUnbreakable(partial), "unbreakable component was not copied");
        require(Component.literal("component-copy-check").equals(partial.get(DataComponents.CUSTOM_NAME)),
            "custom name component was not copied");

        ItemStack empty = pitcher.copyAsFilledStack(partial, 0);
        require(empty.is(CRItems.EMPTY_PAINT_PITCHER.get()), "drained pitcher did not become empty pitcher");
        require(ItemUtils.isUnbreakable(empty), "unbreakable component was lost while draining");
        CustomData remainingData = empty.get(DataComponents.CUSTOM_DATA);
        require(remainingData != null && remainingData.copyTag().getBooleanOr(SMOKE_MARKER, false),
            "unrelated custom data was lost while removing the fill level");
        require(!remainingData.copyTag().contains("FillLevel"), "empty pitcher retained its fill level");
    }

    private static void checkFluidTransfer() {
        TestItemSlot slot = new TestItemSlot(CRItems.EMPTY_PAINT_PITCHER.asStack());
        ContainerItemContext context = ContainerItemContext.ofSingleSlot(slot);
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        require(storage != null, "empty pitcher has no Fabric fluid storage");

        CompoundTag colorTag = PaintFluid.setColor(new CompoundTag(), PalettesColor.RED);
        FluidVariant paint = FluidVariant.of(
            CRFluids.PAINT.get(),
            DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_DATA, CustomData.of(colorTag))
                .build()
        );
        require(
            FluidVariantAttributes.getName(paint).equals(Component.translatable(PalettesColor.RED.getPaintNameId())),
            "paint fluid attributes did not preserve the component color name"
        );
        long amount = PaintPitcherItem.FLUID_PER_LEVEL * 5;

        try (Transaction transaction = Transaction.openOuter()) {
            require(storage.insert(paint, amount, transaction) == amount, "paint insertion amount mismatch");
            transaction.commit();
        }

        ItemStack filled = slot.getResource().toStack();
        PaintPitcherItem redPitcher = CRItems.PAINT_PITCHERS.get(PalettesColor.RED).get();
        require(filled.getItem() == redPitcher, "paint insertion selected the wrong pitcher item");
        require(redPitcher.getLevels(filled) == 5, "paint insertion stored the wrong fill level");

        try (Transaction transaction = Transaction.openOuter()) {
            require(storage.extract(paint, amount, transaction) == amount, "paint extraction amount mismatch");
            transaction.commit();
        }

        require(slot.getResource().toStack().is(CRItems.EMPTY_PAINT_PITCHER.get()),
            "fully drained fluid storage did not produce an empty pitcher");
    }

    private static void checkCannonSplitting() {
        PaintPitcherItem pitcher = CRItems.PAINT_PITCHERS.get(PalettesColor.RED).get();
        ItemStack remaining = new ItemStack(pitcher);
        int totalUsedLevels = 0;
        int shots = 0;

        while (remaining.getItem() instanceof PaintPitcherItem remainingPitcher) {
            int levelsBefore = remainingPitcher.getLevels(remaining);
            PaintPitcherItem.CannonShot shot = remainingPitcher.splitForCannon(remaining);

            require(shot.usedLevels() > 0, "a non-empty pitcher produced an empty cannon shot");
            require(shot.projectile().getItem() == remainingPitcher,
                "cannon shot changed the paint-pitcher item");
            require(remainingPitcher.getLevels(shot.projectile()) == shot.usedLevels(),
                "cannon projectile stored the wrong paint amount");

            int remainingLevels = shot.remainder().getItem() instanceof PaintPitcherItem remainderPitcher
                ? remainderPitcher.getLevels(shot.remainder())
                : 0;
            require(levelsBefore == shot.usedLevels() + remainingLevels,
                "cannon split created or lost paint levels");

            totalUsedLevels += shot.usedLevels();
            remaining = shot.remainder();
            shots++;
            require(shots <= PaintPitcherItem.MAX_LEVELS / PaintPitcherItem.LEVELS_PER_CANNON_SHOT,
                "cannon splitting did not terminate");
        }

        require(shots == 4, "a full pitcher did not produce exactly four cannon shots");
        require(totalUsedLevels == PaintPitcherItem.MAX_LEVELS,
            "cannon shots did not conserve the full pitcher paint amount");
        require(remaining.is(CRItems.EMPTY_PAINT_PITCHER.get()),
            "final cannon split did not return an empty pitcher");
    }

    private static void checkPotatoProjectileRegistration(MinecraftServer server) {
        require(
            CreateRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION.getValue(Railways.asResource("paint"))
                == CRPotatoProjectileTypes.PaintAction.CODEC,
            "paint block-hit action is not registered"
        );

        PotatoCannonProjectileType projectileType = server.registryAccess()
            .lookupOrThrow(CreateRegistryKeys.POTATO_PROJECTILE_TYPE)
            .getValue(Railways.asResource("paint_pitcher"));
        require(projectileType != null, "paint pitcher projectile type did not load from the data pack");
        require(projectileType.onBlockHit().orElse(null) == CRPotatoProjectileTypes.PaintAction.INSTANCE,
            "paint pitcher projectile did not decode the Railways paint block-hit action");
        require(projectileType.items().contains(CRItems.PAINT_PITCHERS.get(PalettesColor.RED).get()
                .builtInRegistryHolder()),
            "paint pitcher projectile type does not contain colored pitchers");
        require(projectileType.items().contains(CRItems.SANDY_PITCHER.get().builtInRegistryHolder()),
            "paint pitcher projectile type does not contain the sandy pitcher");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("Paint pitcher runtime check failed: " + message);
    }

    private static final class TestItemSlot extends SingleVariantStorage<ItemVariant> {
        private TestItemSlot(ItemStack initialStack) {
            variant = ItemVariant.of(initialStack);
            amount = 1;
        }

        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return 1;
        }
    }
}
