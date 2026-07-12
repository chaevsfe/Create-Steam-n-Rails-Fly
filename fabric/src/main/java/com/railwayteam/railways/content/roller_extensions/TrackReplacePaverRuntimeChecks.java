/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.roller_extensions;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.roller_extensions.fabric.TrackReplacePaverImpl;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.zurrtum.create.infrastructure.items.CombinedInvWrapper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

/** Development-only probes for the roller track-replacement transaction. */
public final class TrackReplacePaverRuntimeChecks {
    private static boolean registered;
    private static boolean checked;

    private TrackReplacePaverRuntimeChecks() {
    }

    public static void register() {
        if (registered || !Utils.isDevEnv()) {
            return;
        }
        registered = true;
        ServerLifecycleEvents.SERVER_STARTED.register(TrackReplacePaverRuntimeChecks::run);
    }

    private static void run(MinecraftServer server) {
        if (checked) {
            return;
        }
        checked = true;

        checkStraightStateAndCasing();
        checkCurveMaterialAndCasing();
        checkStorageTransactions(server);
        Railways.LOGGER.info(
            "Roller track-replacement straight, curve, casing, inventory rollback, and creative runtime checks passed"
        );
    }

    private static void checkStraightStateAndCasing() {
        BlockState source = AllBlocks.TRACK.defaultBlockState()
            .setValue(TrackBlock.SHAPE, TrackShape.XO)
            .setValue(TrackBlock.HAS_BE, true)
            .setValue(BlockStateProperties.WATERLOGGED, true);
        BlockState copied = TrackReplacePaver.copySharedProperties(
            source,
            CRBlocks.OAK_TRACK.get().defaultBlockState()
        );
        require(copied.is(CRBlocks.OAK_TRACK.get()), "straight replacement selected the wrong block");
        require(copied.getValue(TrackBlock.SHAPE) == TrackShape.XO, "straight replacement lost track shape");
        require(copied.getValue(TrackBlock.HAS_BE), "straight replacement lost block-entity state");
        require(copied.getValue(BlockStateProperties.WATERLOGGED), "straight replacement lost waterlogging");

        TrackBlockEntity sourceBE = new TrackBlockEntity(BlockPos.ZERO, source);
        TrackBlockEntity targetBE = new TrackBlockEntity(BlockPos.ZERO, copied);
        IHasTrackCasing sourceCasing = (IHasTrackCasing) sourceBE;
        IHasTrackCasing targetCasing = (IHasTrackCasing) targetBE;
        sourceCasing.railways$setTrackCasing(Blocks.OAK_SLAB);
        sourceCasing.railways$setAlternate(true);
        TrackReplacePaver.copyCasing(sourceCasing, targetCasing);
        require(targetCasing.railways$getTrackCasing() == Blocks.OAK_SLAB,
            "straight replacement lost casing");
        require(targetCasing.railways$isAlternate(), "straight replacement lost alternate casing model");
    }

    private static void checkCurveMaterialAndCasing() {
        Couple<BlockPos> positions = Couple.create(BlockPos.ZERO, new BlockPos(4, 0, 4));
        Couple<Vec3> starts = Couple.create(new Vec3(.5, .5, .5), new Vec3(4.5, .5, 4.5));
        Couple<Vec3> axes = Couple.create(new Vec3(1, 0, 0), new Vec3(0, 0, -1));
        Couple<Vec3> normals = Couple.create(new Vec3(0, 1, 0), new Vec3(0, 1, 0));
        BezierConnection primary = new BezierConnection(
            positions, starts, axes, normals, true, false, AllTrackMaterials.ANDESITE
        );
        BezierConnection secondary = primary.secondary();
        IHasTrackCasing primaryCasing = (IHasTrackCasing) primary;
        IHasTrackCasing secondaryCasing = (IHasTrackCasing) secondary;
        primaryCasing.railways$setTrackCasing(Blocks.OAK_SLAB);
        primaryCasing.railways$setAlternate(true);
        secondaryCasing.railways$setTrackCasing(Blocks.OAK_SLAB);
        secondaryCasing.railways$setAlternate(true);

        require(primary.getTrackItemCost() > 0, "curve reported a non-positive replacement cost");
        TrackReplacePaver.replaceConnectionMaterial(primary, secondary, CRTrackMaterials.OAK);
        require(primary.getMaterial() == CRTrackMaterials.OAK && secondary.getMaterial() == CRTrackMaterials.OAK,
            "curve material was not changed at both endpoints");
        require(primaryCasing.railways$getTrackCasing() == Blocks.OAK_SLAB && primaryCasing.railways$isAlternate(),
            "primary curve casing metadata changed with material");
        require(secondaryCasing.railways$getTrackCasing() == Blocks.OAK_SLAB && secondaryCasing.railways$isAlternate(),
            "secondary curve casing metadata changed with material");
    }

    private static void checkStorageTransactions(MinecraftServer server) {
        ItemStack track = CRBlocks.OAK_TRACK.asStack();
        FilterItemStack filter = FilterItemStack.of(track.copy());

        SimpleContainer exactContainer = new SimpleContainer(1);
        exactContainer.setItem(0, track.copyWithCount(3));
        ItemStack exact = TrackReplacePaverImpl.extractFromStorage(
            filter,
            server.overworld(),
            new CombinedInvWrapper(exactContainer),
            false,
            3
        );
        require(exact.getCount() == 3 && exactContainer.getItem(0).isEmpty(),
            "exact track payment was not consumed once");

        SimpleContainer insufficientContainer = new SimpleContainer(1);
        insufficientContainer.setItem(0, track.copyWithCount(2));
        ItemStack insufficient = TrackReplacePaverImpl.extractFromStorage(
            filter,
            server.overworld(),
            new CombinedInvWrapper(insufficientContainer),
            false,
            3
        );
        require(insufficient.isEmpty() && insufficientContainer.getItem(0).getCount() == 2,
            "insufficient track payment was partially consumed");

        SimpleContainer racyContainer = new SimpleContainer(1);
        racyContainer.setItem(0, track.copyWithCount(3));
        ItemStack raced = TrackReplacePaverImpl.extractFromStorage(
            filter,
            server.overworld(),
            new PartialExtractWrapper(racyContainer),
            false,
            3
        );
        require(raced.isEmpty() && racyContainer.getItem(0).getCount() == 3,
            "partial extraction was not rolled back");

        SimpleContainer creativeContainer = new SimpleContainer(1);
        creativeContainer.setItem(0, track.copyWithCount(1));
        ItemStack creative = TrackReplacePaverImpl.extractFromStorage(
            filter,
            server.overworld(),
            new CombinedInvWrapper(creativeContainer),
            true,
            7
        );
        require(creative.getCount() == 7 && creativeContainer.getItem(0).getCount() == 1,
            "creative track supply was consumed or returned the wrong amount");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Roller track-replacement runtime check failed: " + message);
        }
    }

    private static final class PartialExtractWrapper extends CombinedInvWrapper {
        private boolean firstExtraction = true;

        private PartialExtractWrapper(SimpleContainer container) {
            super(container);
        }

        @Override
        public ItemStack extract(Predicate<ItemStack> predicate, int maxAmount, @Nullable Direction side) {
            if (firstExtraction) {
                firstExtraction = false;
                return super.extract(predicate, maxAmount - 1, side);
            }
            return super.extract(predicate, maxAmount, side);
        }
    }
}
