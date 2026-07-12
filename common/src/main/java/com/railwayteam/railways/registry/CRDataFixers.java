/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.base.datafix.CRReferences;
import com.railwayteam.railways.base.datafix.fixes.CompatCherryTrackFix;
import com.railwayteam.railways.base.datafix.fixes.DiagonalHazardStripesFacingFix;
import com.railwayteam.railways.base.datafix.fixes.LocoMetalSmokeboxFacingFix;
import com.railwayteam.railways.base.datafix.fixes.SmokestackPartFix;
import com.railwayteam.railways.base.datafix.fixes.StreamlinedSmokeStackFacingFix;
import com.railwayteam.railways.base.datafix.fixes.UpsideDownMonoBogeyFix;
import com.railwayteam.railways.base.datafix.schemas.V0;
import com.railwayteam.railways.base.datafixerapi.DataFixesInternals;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.smokestack.block.variable.VariableStackPart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.fixes.AddNewChoices;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Set;
import java.util.function.BiFunction;

import static com.railwayteam.railways.base.datafixerapi.DataFixesInternals.baseSchema;

/** Railways' isolated DFU graph. Its version is intentionally independent of vanilla's. */
public final class CRDataFixers {
    private static final BiFunction<Integer, Schema, Schema> SAME_NAMESPACED = NamespacedSchema::new;

    private CRDataFixers() {
    }

    public static void register() {
        if (CRConfigs.getDisableDatafixer()) {
            Railways.LOGGER.warn("[Railways DFU] Registration disabled by config");
            return;
        }

        try {
            DataFixerBuilder builder = new DataFixerBuilder(Railways.DATA_FIXER_VERSION);
            addFixers(builder);

            // A direct executor makes bootstrap deterministic and leaves no worker thread behind.
            DataFixerBuilder.Result result = builder.build();
            Set<com.mojang.datafixers.DSL.TypeReference> optimizedTypes = Set.of(
                References.BLOCK_STATE,
                References.ENTITY,
                References.STRUCTURE,
                References.CHUNK,
                References.ENTITY_CHUNK,
                CRReferences.SAVED_DATA_CREATE_TRACKS
            );
            result.optimize(optimizedTypes, Runnable::run).join();
            DataFixer fixer = result.fixer();
            verifyFixer(fixer);

            DataFixesInternals.get().registerFixer(Railways.DATA_FIXER_VERSION, fixer);
            if (DataFixesInternals.get().getFixerEntry() == null)
                throw new IllegalStateException("Railways DFU bridge rejected the verified fixer");

            Railways.LOGGER.info(
                "[Railways DFU] Schemas V0/V1/V2/V10/V11 and legacy migration probes verified"
            );
        } catch (Throwable throwable) {
            if (throwable instanceof VirtualMachineError error)
                throw error;
            // Loading unmodified data is safer than preventing a world from opening.
            Railways.LOGGER.error(
                "[Railways DFU] Registration or self-test failed; continuing with legacy fixes disabled safely",
                throwable
            );
        }
    }

    private static void addFixers(DataFixerBuilder builder) {
        Schema schemaV0 = builder.addSchema(0, baseSchema(V0::new));
        builder.addFixer(new AddNewChoices(schemaV0, "Added Create contraption entity choices", References.ENTITY));

        Schema schemaV1 = builder.addSchema(1, SAME_NAMESPACED);
        builder.addFixer(new UpsideDownMonoBogeyFix(
            schemaV1,
            "Merge upside-down monorail bogey into the mono bogey blockstate"
        ));

        Schema schemaV2 = builder.addSchema(2, SAME_NAMESPACED);
        builder.addFixer(new CompatCherryTrackFix(schemaV2, "Convert compat cherry tracks to the base cherry track"));
        builder.addFixer(new StreamlinedSmokeStackFacingFix(
            schemaV2,
            "Convert streamlined smokestack axis to facing"
        ));
        builder.addFixer(new LocoMetalSmokeboxFacingFix(
            schemaV2,
            "Convert locometal smokebox axis to facing"
        ));

        Schema schemaV10 = builder.addSchema(10, SAME_NAMESPACED);
        builder.addFixer(new DiagonalHazardStripesFacingFix(
            schemaV10,
            "Convert diagonal hazard stripe axis to facing"
        ));

        Schema schemaV11 = builder.addSchema(11, SAME_NAMESPACED);
        @SuppressWarnings("unchecked")
        Pair<String, VariableStackPart>[] variableStacks = new Pair[] {
            Pair.of("long", VariableStackPart.SINGLE),
            Pair.of("coalburner", VariableStackPart.DOUBLE),
            Pair.of("oilburner", VariableStackPart.DOUBLE),
            Pair.of("streamlined", VariableStackPart.SINGLE),
            Pair.of("woodburner", VariableStackPart.DOUBLE)
        };
        for (Pair<String, VariableStackPart> stack : variableStacks) {
            String blockId = "railways:smokestack_" + stack.getFirst();
            builder.addFixer(new SmokestackPartFix(
                schemaV11,
                "Add variable smokestack part to " + blockId,
                blockId,
                stack.getSecond()
            ));
        }
    }

    private static void verifyFixer(DataFixer fixer) {
        CompoundTag monoBogey = updateBlockState(fixer, 0, "railways:mono_bogey_upside_down", null, null);
        requireName(monoBogey, "railways:mono_bogey");
        requireProperty(monoBogey, "upside_down", "true");

        CompoundTag cherry = updateBlockState(fixer, 1, "railways:track_biomesoplenty_cherry", null, null);
        requireName(cherry, "railways:track_cherry");

        CompoundTag smokebox = updateBlockState(
            fixer,
            1,
            "railways:red_locometal_smokebox",
            "axis",
            "x"
        );
        requireProperty(smokebox, "facing", "east");

        CompoundTag hazard = updateBlockState(
            fixer,
            2,
            "railways:red_hazard_stripes_diagonal_on_black",
            "axis",
            "z"
        );
        requireProperty(hazard, "facing", "north");

        CompoundTag smokestack = updateBlockState(fixer, 10, "railways:smokestack_long", null, null);
        requireProperty(smokestack, "part", VariableStackPart.SINGLE.getSerializedName());

        verifyTracksSavedDataTraversal(fixer);
    }

    private static CompoundTag updateBlockState(
        DataFixer fixer,
        int fromVersion,
        String name,
        String property,
        String value
    ) {
        CompoundTag input = blockState(name, property, value);
        return (CompoundTag) fixer.update(
            References.BLOCK_STATE,
            new Dynamic<>(NbtOps.INSTANCE, input),
            fromVersion,
            Railways.DATA_FIXER_VERSION
        ).getValue();
    }

    private static CompoundTag blockState(String name, String property, String value) {
        CompoundTag state = new CompoundTag();
        state.putString("Name", name);
        CompoundTag properties = new CompoundTag();
        if (property != null && value != null)
            properties.putString(property, value);
        state.put("Properties", properties);
        return state;
    }

    private static void verifyTracksSavedDataTraversal(DataFixer fixer) {
        ListTag palette = new ListTag();
        palette.add(blockState("railways:mono_bogey_upside_down", null, null));

        CompoundTag blocks = new CompoundTag();
        blocks.put("Palette", palette);
        CompoundTag contraption = new CompoundTag();
        contraption.put("Blocks", blocks);
        CompoundTag entity = new CompoundTag();
        entity.putString("id", "create:carriage_contraption");
        entity.put("Contraption", contraption);
        CompoundTag carriage = new CompoundTag();
        carriage.put("Entity", entity);
        ListTag carriages = new ListTag();
        carriages.add(carriage);
        CompoundTag train = new CompoundTag();
        train.put("Carriages", carriages);
        ListTag trains = new ListTag();
        trains.add(train);
        CompoundTag data = new CompoundTag();
        data.put("Trains", trains);
        CompoundTag root = new CompoundTag();
        root.put("data", data);

        CompoundTag fixed = (CompoundTag) fixer.update(
            CRReferences.SAVED_DATA_CREATE_TRACKS,
            new Dynamic<>(NbtOps.INSTANCE, root),
            0,
            Railways.DATA_FIXER_VERSION
        ).getValue();
        CompoundTag fixedState = fixed.getCompoundOrEmpty("data")
            .getListOrEmpty("Trains").getCompoundOrEmpty(0)
            .getListOrEmpty("Carriages").getCompoundOrEmpty(0)
            .getCompoundOrEmpty("Entity")
            .getCompoundOrEmpty("Contraption")
            .getCompoundOrEmpty("Blocks")
            .getListOrEmpty("Palette").getCompoundOrEmpty(0);
        requireName(fixedState, "railways:mono_bogey");
        requireProperty(fixedState, "upside_down", "true");
    }

    private static void requireName(CompoundTag state, String expected) {
        String actual = state.getStringOr("Name", "");
        if (!expected.equals(actual))
            throw new IllegalStateException("DFU probe expected block " + expected + " but got " + actual);
    }

    private static void requireProperty(CompoundTag state, String key, String expected) {
        String actual = state.getCompoundOrEmpty("Properties").getStringOr(key, "");
        if (!expected.equals(actual))
            throw new IllegalStateException(
                "DFU probe expected property " + key + "=" + expected + " but got " + actual
            );
    }
}
