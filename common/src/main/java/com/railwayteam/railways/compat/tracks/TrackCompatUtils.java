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

import com.google.common.collect.ImmutableSet;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.compat.Mods;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.mixin.AccessorTrackMaterialFactory;
import com.railwayteam.railways.multiloader.CommonTags;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.AllTags;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockItem;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackMaterialFactory;
import com.zurrtum.create.foundation.data.CreateRegistrate;
import com.zurrtum.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import static com.railwayteam.railways.base.data.CRTagGen.addOptionalTag;

public abstract class TrackCompatUtils {

    public static final Set<String> TRACK_COMPAT_MODS = ImmutableSet.of(
            "hexcasting",
            "byg", // Oh The Biomes You'll Go,
            "blue_skies",
            "twilightforest",
            "biomesoplenty",
            "natures_spirit",
            "create_dd", // Dreams 'n' Desires
            "quark",
            "tfc" // TerraFirmaCraft
    );

    public static boolean anyLoaded() {
        if (GenericTrackCompat.isDataGen() || CRConfigs.common().registerMissingTracks.get())
            return true;
        for (String mod : TRACK_COMPAT_MODS) {
            if (Mods.valueOf(mod.toUpperCase(Locale.ROOT)).isLoaded)
                return true;
        }
        return false;
    }

    @ApiStatus.Internal
    public static boolean mixinSkipLootLoading(Identifier Identifier) {
        if (Identifier.getNamespace().equals(Railways.MOD_ID)) {
            for (String compatMod : TRACK_COMPAT_MODS) {
                if (Identifier.getPath().startsWith("blocks/track_"+compatMod)) {
                    return !GenericTrackCompat.get(compatMod).shouldRegisterMissing();
                }
            }
        }
        return false;
    }

    private static final CreateRegistrate REGISTRATE = Railways.registrate();

    public static BlockEntry<TrackBlock> makeTrack(TrackMaterial material) {
        return makeTrack(material, (ctx, prov) -> {});
    }

    public static BlockEntry<TrackBlock> makeTrack(TrackMaterial material, boolean hideInCreativeTabs) {
        return makeTrack(material, (ctx, prov) -> {}, (t) -> {}, (p) -> p, hideInCreativeTabs);
    }

    public static BlockEntry<TrackBlock> makeTrack(TrackMaterial material, NonNullBiConsumer<DataGenContext<Block, TrackBlock>, RegistrateBlockstateProvider> blockstateGen) {
        return makeTrack(material, blockstateGen, (t) -> {});
    }

    public static BlockEntry<TrackBlock> makeTrack(TrackMaterial material, NonNullBiConsumer<DataGenContext<Block, TrackBlock>, RegistrateBlockstateProvider> blockstateGen, NonNullConsumer<? super TrackBlock> onRegister) {
        return makeTrack(material, blockstateGen, onRegister, (p) -> p);
    }

    public static BlockEntry<TrackBlock> makeTrack(TrackMaterial material, NonNullBiConsumer<DataGenContext<Block, TrackBlock>, RegistrateBlockstateProvider> blockstateGen, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> collectProperties) {
        return makeTrack(material, blockstateGen, (t) -> {}, collectProperties);
    }

    public static BlockEntry<TrackBlock> makeTrack(TrackMaterial material, NonNullBiConsumer<DataGenContext<Block, TrackBlock>, RegistrateBlockstateProvider> blockstateGen, NonNullConsumer<? super TrackBlock> onRegister, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> collectProperties) {
        return makeTrack(material, blockstateGen, onRegister, collectProperties, false);
    }

    public static BlockEntry<TrackBlock> makeTrack(TrackMaterial material, NonNullBiConsumer<DataGenContext<Block, TrackBlock>, RegistrateBlockstateProvider> blockstateGen, NonNullConsumer<? super TrackBlock> onRegister, Function<BlockBehaviour.Properties, BlockBehaviour.Properties> collectProperties, boolean hideInCreativeTabs) {
        String owningMod = CRTrackMaterials.id(material).getNamespace();
        String name = "track_" + owningMod + "_" + material.getId().getPath();

        addOptionalTag(Railways.asResource(name), AllTags.AllBlockTags.TRACKS.tag,
                CommonTags.RELOCATION_NOT_SUPPORTED.forge, CommonTags.RELOCATION_NOT_SUPPORTED.fabric,
                BlockTags.MINEABLE_WITH_PICKAXE); // pickaxe-mineable tag is moved here as Registrate cannot add optional tag in BlockBuilder
        if (CRTrackMaterials.getType(material) != CRTrackMaterials.CRTrackType.MONORAIL)
            addOptionalTag(Railways.asResource(name), AllTags.AllBlockTags.GIRDABLE_TRACKS.tag);

        return REGISTRATE.block(name, p -> new TrackBlock(p, material))
            .initialProperties(SharedProperties::stone)
            .properties(p -> collectProperties.apply(p)
                .mapColor(MapColor.METAL)
                .strength(0.8F)
                .sound(SoundType.METAL)
                .noOcclusion())
            .lang(CRTrackMaterials.langName(material) + " Train Track")
            .onRegister(onRegister)
            .item(TrackBlockItem::new)
            .removeTab(hideInCreativeTabs ? null : CreativeModeTabs.SEARCH)
            .build()
            .register();
    }

    public static TrackMaterial buildCompatModels(GenericTrackCompat trackCompat, TrackMaterialFactory factory) {
        return factory.build();
    }
}
