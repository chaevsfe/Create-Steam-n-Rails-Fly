/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.compat.tracks;

import com.railwayteam.railways.ModSetup;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.compat.Mods;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.multiloader.CommonTags;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType;
import com.railwayteam.railways.util.TextUtils;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyItem;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.railwayteam.railways.Railways.registrate;
import static com.railwayteam.railways.compat.tracks.TrackCompatUtils.buildCompatModels;
import static com.railwayteam.railways.compat.tracks.TrackCompatUtils.makeTrack;
import static com.railwayteam.railways.registry.CRItems.ITEM_INCOMPLETE_TRACK;
import static com.zurrtum.create.content.trains.track.TrackMaterialFactory.make;

public class GenericTrackCompat {
    private static final Map<String, GenericTrackCompat> ALL = new HashMap<>();

    public final Mods mod;
    public final String modid;
    public boolean modLoaded;

    public GenericTrackCompat(Mods mod) {
        this.mod = mod;
        this.modid = mod.asId();
        this.modLoaded = mod.isLoaded;
        ALL.put(modid, this);
    }

    public static GenericTrackCompat get(String modid) {
        return ALL.get(modid);
    }

    protected final Map<String, TrackMaterial> MATERIALS = new HashMap<>();
    protected final Map<String, NonNullSupplier<? extends TrackBlock>> BLOCKS = new HashMap<>();

    public static boolean isDataGen() {
        return Utils.isEnvVarTrue("DATAGEN");
    }

    static {
        ModSetup.useTracksTab();
    }

    // If tracks/materials should still be registered if the base block is missing
    protected final boolean shouldRegisterMissing() {
        return isDataGen() || CRConfigs.getRegisterMissingTracks() || modLoaded;
    }

    public void register(String... names) {
        for (String name : names) {
            Optional<Block> baseBlock = BuiltInRegistries.BLOCK.getOptional(getSlabLocation(name));
            if (baseBlock.isEmpty()) {
                if (!shouldRegisterMissing()) continue; // skip if we shouldn't register tracks for missing base blocks
                if (isDataGen() || Utils.isDevEnv())
                    Railways.LOGGER.error("Failed to locate base block at {} for {}", getSlabLocation(name), asResource(name));
            }
            // standard gauge
            Object sleeper = baseBlock.<Object>map(Ingredient::of).orElseGet(() -> SoftIngredient.of(getSlabLocation(name)));
            TrackMaterial standardMaterial = buildCompatModels(this, make(asResource(name))
                .lang(langName(name))
                .block(() -> BLOCKS.get(name))
                .particle(asResource("block/track/"+name+"/standard_track_crossing_"+name))
                .sleeper(sleeper)
                .rails(Items.IRON_NUGGET)
            );
            MATERIALS.put(name, standardMaterial);

            NonNullSupplier<TrackBlock> standardBlock = makeTrack(standardMaterial);
            BLOCKS.put(name, standardBlock);

            // wide gauge
            TrackMaterial wideMaterial = wideVariant(standardMaterial);
            MATERIALS.put(name+"_wide", wideMaterial);
            CRTrackMaterials.WIDE_GAUGE.put(standardMaterial, wideMaterial);
            CRTrackMaterials.WIDE_GAUGE_REVERSE.put(wideMaterial, standardMaterial);

            NonNullSupplier<TrackBlock> wideBlock = makeTrack(wideMaterial);
            CRBlocks.WIDE_GAUGE_TRACKS.put(wideMaterial, wideBlock);
            BLOCKS.put(name+"_wide", wideBlock);

            // narrow gauge
            TrackMaterial narrowMaterial = narrowVariant(standardMaterial);
            MATERIALS.put(name+"_narrow", narrowMaterial);
            CRTrackMaterials.NARROW_GAUGE.put(standardMaterial, narrowMaterial);
            CRTrackMaterials.NARROW_GAUGE_REVERSE.put(narrowMaterial, standardMaterial);

            NonNullSupplier<TrackBlock> narrowBlock = makeTrack(narrowMaterial);
            CRBlocks.NARROW_GAUGE_TRACKS.put(narrowMaterial, narrowBlock);
            BLOCKS.put(name+"_narrow", narrowBlock);
        }
    }

    protected String langName(String name) {
        return TextUtils.titleCaseConversion(name.replace('_', ' '));
    }

    protected Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(modid, path);
    }

    protected Identifier getSlabLocation(String name) {
        return asResource(name+"_slab");
    }

    protected String getLang(String name) {
        return name;
    }

    protected Ingredient getIngredientForRail() {
        return Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, CommonTags.IRON_NUGGETS.tag));
    }

    private TrackMaterial wideVariant(TrackMaterial material) {
        String path = CRTrackMaterials.id(material).getPath() + "_wide";
        return buildCompatModels(this, make(asResource(path))
            .lang("Wide " + CRTrackMaterials.langName(material))
            .trackType(CRTrackType.WIDE_GAUGE)
            .block(() -> CRBlocks.WIDE_GAUGE_TRACKS.get(CRTrackMaterials.WIDE_GAUGE.get(material)))
            .particle(CRTrackMaterials.particle(material))
            .noRecipeGen());
    }

    private TrackMaterial narrowVariant(TrackMaterial material) {
        String path = CRTrackMaterials.id(material).getPath() + "_narrow";
        return buildCompatModels(this, make(asResource(path))
            .lang("Narrow " + CRTrackMaterials.langName(material))
            .trackType(CRTrackType.NARROW_GAUGE)
            .block(() -> CRBlocks.NARROW_GAUGE_TRACKS.get(CRTrackMaterials.NARROW_GAUGE.get(material)))
            .particle(CRTrackMaterials.particle(material))
            .noRecipeGen());
    }
}
