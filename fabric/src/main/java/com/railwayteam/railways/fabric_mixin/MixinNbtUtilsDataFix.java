/*
 * Copyright 2022 QuiltMC
 * Modified by the Steam 'n' Rails (Railways) team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.base.datafix.fixes.CompatCherryTrackFix;
import com.railwayteam.railways.base.datafixerapi.DataFixesInternals;
import net.minecraft.core.HolderGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Version stamping plus an idempotent last-resort path for all known Railways blockstate fixes. */
@Mixin(NbtUtils.class)
public abstract class MixinNbtUtilsDataFix {
    @Inject(
        method = "addDataVersion(Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;",
        at = @At("HEAD")
    )
    private static void railways$addModDataVersion(
        CompoundTag tag,
        int dataVersion,
        CallbackInfoReturnable<CompoundTag> cir
    ) {
        DataFixesInternals.get().addModDataVersions(tag);
    }

    @Inject(
        method = "readBlockState(Lnet/minecraft/core/HolderGetter;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/level/block/state/BlockState;",
        at = @At("HEAD")
    )
    private static void railways$repairLegacyBlockState(
        HolderGetter<Block> blockGetter,
        CompoundTag tag,
        CallbackInfoReturnable<BlockState> cir
    ) {
        String name = tag.getStringOr("Name", "");
        if (name.isEmpty())
            return;

        if ("railways:mono_bogey_upside_down".equals(name)) {
            tag.putString("Name", "railways:mono_bogey");
            railways$properties(tag).putString("upside_down", "true");
            name = "railways:mono_bogey";
        }

        if (CompatCherryTrackFix.standardCherryOld.contains(name)) {
            tag.putString("Name", "railways:track_cherry");
        } else if (CompatCherryTrackFix.wideCherryOld.contains(name)) {
            tag.putString("Name", "railways:track_cherry_wide");
        } else if (CompatCherryTrackFix.narrowCherryOld.contains(name)) {
            tag.putString("Name", "railways:track_cherry_narrow");
        }

        boolean axisToFacing = name.startsWith("railways:") && (
            "railways:smokestack_streamlined".equals(name)
                || "railways:locometal_smokebox".equals(name)
                || name.endsWith("_locometal_smokebox")
                || name.endsWith("_hazard_stripes_diagonal_on_black")
                || name.endsWith("_hazard_stripes_diagonal_on_white")
        );
        if (axisToFacing)
            railways$axisToFacing(tag);

        String defaultPart = switch (name) {
            case "railways:smokestack_long", "railways:smokestack_streamlined" -> "single";
            case "railways:smokestack_coalburner", "railways:smokestack_oilburner",
                 "railways:smokestack_woodburner" -> "double";
            default -> null;
        };
        if (defaultPart != null) {
            CompoundTag properties = railways$properties(tag);
            if (!properties.contains("part"))
                properties.putString("part", defaultPart);
        }
    }

    @Unique
    private static CompoundTag railways$properties(CompoundTag tag) {
        CompoundTag properties = tag.getCompoundOrEmpty("Properties");
        tag.put("Properties", properties);
        return properties;
    }

    @Unique
    private static void railways$axisToFacing(CompoundTag tag) {
        CompoundTag properties = railways$properties(tag);
        String facing = switch (properties.getStringOr("axis", "")) {
            case "x" -> "east";
            case "y" -> "up";
            case "z" -> "north";
            default -> null;
        };
        if (facing == null)
            return;
        properties.putString("facing", facing);
        properties.remove("axis");
    }
}
