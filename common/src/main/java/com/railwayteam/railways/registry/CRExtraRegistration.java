package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class CRExtraRegistration {
    private static boolean registered;

    public static void register() {
        if (registered)
            throw new IllegalStateException("Railways extra display registrations were applied more than once");

        addBlockSource(CRBlocks.TRACK_COUPLER.get(), CRDisplaySources.TRACK_COUPLER_INFO.get());
        addBlockSource(CRBlocks.ANDESITE_SWITCH.get(), CRDisplaySources.TRACK_SWITCH.get());
        addBlockSource(CRBlocks.BRASS_SWITCH.get(), CRDisplaySources.TRACK_SWITCH.get());
        addBlockEntitySource(AllBlockEntityTypes.TRACK_SIGNAL, CRDisplaySources.SIGNAL.get());
        DisplayTarget.BY_BLOCK.register(CRBlocks.SEMAPHORE.get(), CRDisplayTargets.SEMAPHORE.get());

        // Multi registries cache lookups, so make bindings visible even if another initializer queried early.
        DisplaySource.BY_BLOCK.invalidate();
        DisplaySource.BY_BLOCK_ENTITY.invalidate();
        verifyDisplayRegistrations();
        registered = true;

        if (Utils.isDevEnv())
            Railways.LOGGER.info("Display source/target registries and block bindings verified");
    }

    private static void addBlockSource(Block block, DisplaySource source) {
        if (DisplaySource.BY_BLOCK.get(block).contains(source))
            throw new IllegalStateException("Display source " + source + " is already bound to block " + block);
        DisplaySource.BY_BLOCK.add(block, source);
    }

    private static void addBlockEntitySource(BlockEntityType<?> type, DisplaySource source) {
        if (DisplaySource.BY_BLOCK_ENTITY.get(type).contains(source))
            throw new IllegalStateException("Display source " + source + " is already bound to block entity " + type);
        DisplaySource.BY_BLOCK_ENTITY.add(type, source);
    }

    private static void verifyDisplayRegistrations() {
        if (CreateRegistries.DISPLAY_SOURCE.getValue(CRDisplaySources.TRACK_COUPLER_INFO.getId())
            != CRDisplaySources.TRACK_COUPLER_INFO.get())
            throw new IllegalStateException("Track coupler display source is missing from Create's registry");
        if (CreateRegistries.DISPLAY_SOURCE.getValue(CRDisplaySources.TRACK_SWITCH.getId())
            != CRDisplaySources.TRACK_SWITCH.get())
            throw new IllegalStateException("Track switch display source is missing from Create's registry");
        if (CreateRegistries.DISPLAY_SOURCE.getValue(CRDisplaySources.SIGNAL.getId()) != CRDisplaySources.SIGNAL.get())
            throw new IllegalStateException("Signal display source is missing from Create's registry");
        if (CreateRegistries.DISPLAY_TARGET.getValue(CRDisplayTargets.SEMAPHORE.getId())
            != CRDisplayTargets.SEMAPHORE.get())
            throw new IllegalStateException("Semaphore display target is missing from Create's registry");

        if (!DisplaySource.BY_BLOCK.get(CRBlocks.TRACK_COUPLER.get()).contains(CRDisplaySources.TRACK_COUPLER_INFO.get()))
            throw new IllegalStateException("Track coupler display source is not bound to its block");
        if (!DisplaySource.BY_BLOCK.get(CRBlocks.ANDESITE_SWITCH.get()).contains(CRDisplaySources.TRACK_SWITCH.get())
            || !DisplaySource.BY_BLOCK.get(CRBlocks.BRASS_SWITCH.get()).contains(CRDisplaySources.TRACK_SWITCH.get()))
            throw new IllegalStateException("Track switch display source is not bound to both switch blocks");
        if (!DisplaySource.BY_BLOCK_ENTITY.get(AllBlockEntityTypes.TRACK_SIGNAL).contains(CRDisplaySources.SIGNAL.get()))
            throw new IllegalStateException("Signal display source is not bound to Create's track signal block entity");
        if (DisplayTarget.BY_BLOCK.get(CRBlocks.SEMAPHORE.get()) != CRDisplayTargets.SEMAPHORE.get())
            throw new IllegalStateException("Semaphore display target is not bound to its block");
    }
}
