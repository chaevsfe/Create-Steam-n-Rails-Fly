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

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.custom_tracks.NoCollisionCustomTrackBlock;
import com.railwayteam.railways.content.custom_tracks.monorail.MonorailTrackBlock;
import com.railwayteam.railways.content.custom_tracks.narrow_gauge.NarrowGaugeTrackBlock;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomTrackBlock;
import com.railwayteam.railways.content.custom_tracks.wide_gauge.WideGaugeTrackBlock;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.zurrtum.create.content.trains.track.TrackMaterialFactory.make;

public class CRTrackMaterials {
    private static final Map<TrackMaterial, Identifier> TYPES = new HashMap<>();
    private static final Map<TrackMaterial, Identifier> PARTICLES = new HashMap<>();
    private static final Map<TrackMaterial, String> LANG_NAMES = new HashMap<>();

    public static final TrackMaterial
        ACACIA = make(Railways.asResource("acacia"))
            .lang("Acacia")
            .block(() -> CRBlocks.ACACIA_TRACK)
            .particle(Identifier.parse("block/acacia_planks"))
            .sleeper(Blocks.ACACIA_SLAB)
            .standardModels()
            .build(),
        BIRCH = make(Railways.asResource("birch"))
            .lang("Birch")
            .block(() -> CRBlocks.BIRCH_TRACK)
            .particle(Identifier.parse("block/birch_planks"))
            .sleeper(Blocks.BIRCH_SLAB)
            .standardModels()
            .build(),
        CRIMSON = make(Railways.asResource("crimson"))
            .lang("Crimson")
            .block(() -> CRBlocks.CRIMSON_TRACK)
            .particle(Identifier.parse("block/crimson_planks"))
            .sleeper(Blocks.CRIMSON_SLAB)
            .rails(Items.GOLD_NUGGET)
            .standardModels()
            .build(),
        DARK_OAK = make(Railways.asResource("dark_oak"))
            .lang("Dark Oak")
            .block(() -> CRBlocks.DARK_OAK_TRACK)
            .particle(Identifier.parse("block/dark_oak_planks"))
            .sleeper(Blocks.DARK_OAK_SLAB)
            .standardModels()
            .build(),
        JUNGLE = make(Railways.asResource("jungle"))
            .lang("Jungle")
            .block(() -> CRBlocks.JUNGLE_TRACK)
            .particle(Identifier.parse("block/jungle_planks"))
            .sleeper(Blocks.JUNGLE_SLAB)
            .standardModels()
            .build(),
        OAK = make(Railways.asResource("oak"))
            .lang("Oak")
            .block(() -> CRBlocks.OAK_TRACK)
            .particle(Identifier.parse("block/oak_planks"))
            .sleeper(Blocks.OAK_SLAB)
            .standardModels()
            .build(),
        SPRUCE = make(Railways.asResource("spruce"))
            .lang("Spruce")
            .block(() -> CRBlocks.SPRUCE_TRACK)
            .particle(Identifier.parse("block/spruce_planks"))
            .sleeper(Blocks.SPRUCE_SLAB)
            .standardModels()
            .build(),
        WARPED = make(Railways.asResource("warped"))
            .lang("Warped")
            .block(() -> CRBlocks.WARPED_TRACK)
            .particle(Identifier.parse("block/warped_planks"))
            .sleeper(Blocks.WARPED_SLAB)
            .rails(Items.GOLD_NUGGET)
            .standardModels()
            .build(),
        BLACKSTONE = make(Railways.asResource("blackstone"))
            .lang("Blackstone")
            .block(() -> CRBlocks.BLACKSTONE_TRACK)
            .particle(Identifier.parse("block/blackstone"))
            .sleeper(Blocks.BLACKSTONE_SLAB)
            .rails(Items.GOLD_NUGGET)
            .standardModels()
            .build(),
        MANGROVE = make(Railways.asResource("mangrove"))
            .lang("Mangrove")
            .block(() -> CRBlocks.MANGROVE_TRACK)
            .particle(Identifier.parse("block/mangrove_planks"))
            .sleeper(Blocks.MANGROVE_SLAB)
            .standardModels()
            .build(),
        CHERRY = make(Railways.asResource("cherry"))
            .lang("Cherry")
            .block(() -> CRBlocks.CHERRY_TRACK)
            .particle(Identifier.parse("block/cherry_planks"))
            .sleeper(Blocks.CHERRY_SLAB)
            .standardModels()
            .build(),
        BAMBOO = make(Railways.asResource("bamboo"))
            .lang("Bamboo")
            .block(() -> CRBlocks.BAMBOO_TRACK)
            .particle(Identifier.parse("block/bamboo_block"))
            .sleeper(Items.BAMBOO)
            .standardModels()
            .build(),
        STRIPPED_BAMBOO = make(Railways.asResource("stripped_bamboo"))
            .lang("Stripped Bamboo")
            .block(() -> CRBlocks.STRIPPED_BAMBOO_TRACK)
            .particle(Identifier.parse("block/bamboo_planks"))
            .sleeper(Blocks.BAMBOO_SLAB)
            .standardModels()
            .build(),
        MONORAIL = make(Railways.asResource("monorail"))
            .lang("Monorail")
            .block(() -> CRBlocks.MONORAIL_TRACK)
            .particle(Railways.asResource("block/monorail/monorail"))
            .trackType(CRTrackMaterials.CRTrackType.MONORAIL)
            .noRecipeGen()
            .build(),
        ENDER = make(Railways.asResource("ender"))
            .lang("Ender")
            .block(() -> CRBlocks.ENDER_TRACK)
            .particle(Identifier.parse("block/end_stone"))
            .sleeper(Blocks.END_STONE_BRICK_SLAB)
            .standardModels()
            .build(),
        TIELESS = make(Railways.asResource("tieless"))
            .lang("Tieless")
            .block(() -> CRBlocks.TIELESS_TRACK)
            .particle(Identifier.parse("block/glass"))
            .sleeper(Blocks.GLASS_PANE)
            .customBlockFactory(NoCollisionCustomTrackBlock::new)
            .standardModels()
            .build(),
        PHANTOM = make(Railways.asResource("phantom"))
            .lang("Phantom")
            .block(() -> CRBlocks.PHANTOM_TRACK)
            .particle(Identifier.parse("block/glass"))
            .noRecipeGen()
            .trackType(CRTrackType.UNIVERSAL)
            .customBlockFactory(PhantomTrackBlock::new)
            .standardModels()
            .build(),
        WIDE_GAUGE_ANDESITE = wideVariant(AllTrackMaterials.ANDESITE),
        NARROW_GAUGE_ANDESITE = narrowVariant(AllTrackMaterials.ANDESITE)
        ;

    public static final Map<TrackMaterial, TrackMaterial> WIDE_GAUGE = new HashMap<>();
    public static final Map<TrackMaterial, TrackMaterial> WIDE_GAUGE_REVERSE = new HashMap<>();

    public static final Map<TrackMaterial, TrackMaterial> NARROW_GAUGE = new HashMap<>();
    public static final Map<TrackMaterial, TrackMaterial> NARROW_GAUGE_REVERSE = new HashMap<>();

    static {
        registerMeta(AllTrackMaterials.ANDESITE, CRTrackMaterials.CRTrackType.STANDARD, Identifier.fromNamespaceAndPath("create", "block/track/andesite"), "Andesite");

        WIDE_GAUGE.put(AllTrackMaterials.ANDESITE, WIDE_GAUGE_ANDESITE);
        WIDE_GAUGE_REVERSE.put(WIDE_GAUGE_ANDESITE, AllTrackMaterials.ANDESITE);
        for (TrackMaterial baseMaterial : allFromMod(Railways.MOD_ID)) {
            if (getType(baseMaterial) != CRTrackMaterials.CRTrackType.STANDARD)
                continue;

            TrackMaterial wideMaterial = wideVariant(baseMaterial);
            WIDE_GAUGE.put(baseMaterial, wideMaterial);
            WIDE_GAUGE_REVERSE.put(wideMaterial, baseMaterial);
        }

        NARROW_GAUGE.put(AllTrackMaterials.ANDESITE, NARROW_GAUGE_ANDESITE);
        NARROW_GAUGE_REVERSE.put(NARROW_GAUGE_ANDESITE, AllTrackMaterials.ANDESITE);
        for (TrackMaterial baseMaterial : allFromMod(Railways.MOD_ID)) {
            if (getType(baseMaterial) != CRTrackMaterials.CRTrackType.STANDARD)
                continue;

            TrackMaterial narrowMaterial = narrowVariant(baseMaterial);
            NARROW_GAUGE.put(baseMaterial, narrowMaterial);
            NARROW_GAUGE_REVERSE.put(narrowMaterial, baseMaterial);
        }
    }

    public static TrackMaterial getWide(TrackMaterial material) {
        return WIDE_GAUGE.get(material);
    }

    @Nullable
    public static TrackMaterial getBaseFromWide(TrackMaterial material) {
        if (!WIDE_GAUGE_REVERSE.containsKey(material))
            return null;
        return WIDE_GAUGE_REVERSE.get(material);
    }

    private static TrackMaterial wideVariant(TrackMaterial material) {
        String path = "";
        if (!id(material).getNamespace().equals(Railways.MOD_ID))
            path = id(material).getNamespace() + "_";
        path += id(material).getPath() + "_wide";
        return make(Railways.asResource(path))
            .lang("Wide " + langName(material))
            .trackType(CRTrackType.WIDE_GAUGE)
            .block(() -> CRBlocks.WIDE_GAUGE_TRACKS.get(WIDE_GAUGE.get(material)))
            .particle(particle(material))
            .noRecipeGen()
            .standardModels()
            .build();
    }

    public static TrackMaterial getNarrow(TrackMaterial material) {
        return NARROW_GAUGE.get(material);
    }

    @Nullable
    public static TrackMaterial getBaseFromNarrow(TrackMaterial material) {
        if (!NARROW_GAUGE_REVERSE.containsKey(material))
            return null;
        return NARROW_GAUGE_REVERSE.get(material);
    }

    private static TrackMaterial narrowVariant(TrackMaterial material) {
        String path = "";
        if (!id(material).getNamespace().equals(Railways.MOD_ID))
            path = id(material).getNamespace() + "_";
        path += id(material).getPath() + "_narrow";
        return make(Railways.asResource(path))
            .lang("Narrow " + langName(material))
            .trackType(CRTrackType.NARROW_GAUGE)
            .block(() -> CRBlocks.NARROW_GAUGE_TRACKS.get(NARROW_GAUGE.get(material)))
            .particle(particle(material))
            .noRecipeGen()
            .standardModels()
            .build();
    }

    public static Identifier id(TrackMaterial material) {
        return material.getId();
    }

    public static String langName(TrackMaterial material) {
        return LANG_NAMES.computeIfAbsent(material, m -> {
            String path = id(m).getPath().replace('_', ' ');
            StringBuilder out = new StringBuilder(path.length());
            boolean upper = true;
            for (char c : path.toCharArray()) {
                out.append(upper ? Character.toUpperCase(c) : c);
                upper = c == ' ';
            }
            return out.toString();
        });
    }

    public static Identifier particle(TrackMaterial material) {
        return PARTICLES.computeIfAbsent(material, m -> id(m).withPrefix("block/track/"));
    }

    public static String resourceName(TrackMaterial material) {
        return id(material).getPath();
    }

    public static Identifier getType(TrackMaterial material) {
        return TYPES.getOrDefault(material, CRTrackMaterials.CRTrackType.STANDARD);
    }

    public static Collection<TrackMaterial> allFromMod(String modid) {
        return TrackMaterial.ALL.values().stream()
            .filter(material -> id(material).getNamespace().equals(modid))
            .toList();
    }

    public static void registerMeta(TrackMaterial material, Identifier type, Identifier particle, String langName) {
        TYPES.put(material, type);
        PARTICLES.put(material, particle);
        LANG_NAMES.put(material, langName);
    }

    public static class CRTrackType {
        public static final Identifier STANDARD = AllTrackMaterials.ANDESITE.getId();
        public static final Identifier MONORAIL = Railways.asResource("monorail");
        public static final Identifier WIDE_GAUGE = Railways.asResource("wide_gauge");
        public static final Identifier NARROW_GAUGE = Railways.asResource("narrow_gauge");
        public static final Identifier UNIVERSAL = Railways.asResource("universal");
    }

    public static void register() {}

    public static void addToBlockEntityType(TrackBlock block) {
        // Create Fly handles its track block entity registration internally on 1.21.11.
    }
}
