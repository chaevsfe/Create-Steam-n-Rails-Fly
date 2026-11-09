/*
 * Steam 'n' Rails
 * Copyright (c) 2024 The Railways Team
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

package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.annotation.multiloader.ImplClass;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRCreativeModeTabs.RegistrateDisplayItemsGenerator;
import com.railwayteam.railways.registry.CRCreativeModeTabs.TabInfo;
import com.railwayteam.railways.registry.CRCreativeModeTabs.Tabs;
import com.railwayteam.railways.registry.CRPalettes;
import com.railwayteam.railways.shim.registrate.util.entry.RegistryEntry;
import com.railwayteam.railways.shim.create.foundation.data.CreateRegistrate;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;

import java.util.function.Supplier;

import static com.railwayteam.railways.registry.CRItems.ITEM_CONDUCTOR_CAP;

@ImplClass
public class CRCreativeModeTabsImpl {

    private static final TabInfo MAIN_TAB = register("main",
        () -> FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.railways"))
            .icon(() -> ITEM_CONDUCTOR_CAP.get(DyeColor.BLUE).asStack())
            .displayItems(new RegistrateDisplayItemsGenerator(Tabs.MAIN))
            .build());

    @SuppressWarnings("Convert2MethodRef")
    private static final TabInfo TRACKS_TAB = register("tracks",
        () -> FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.railways_tracks"))
            .icon(() -> CRBlocks.DARK_OAK_TRACK.asStack())
            .displayItems(new RegistrateDisplayItemsGenerator(Tabs.TRACK))
            .build());

    private static final TabInfo PALETTES_TAB = register("palettes",
            () -> FabricCreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.railways_palettes"))
                    .icon(() -> CRPalettes.Styles.BOILER.get(PalettesColor.RED).asStack())
                    .displayItems(new RegistrateDisplayItemsGenerator(Tabs.PALETTES))
                    .build());

    public static ResourceKey<CreativeModeTab> getBaseTabKey() {
        return MAIN_TAB.key();
    }

    public static ResourceKey<CreativeModeTab> getTracksTabKey() {
        return TRACKS_TAB.key();
    }

    private static TabInfo register(String name, Supplier<CreativeModeTab> supplier) {
        Identifier id = Railways.asResource(name);
        ResourceKey<CreativeModeTab> key = ResourceKey.create(Registries.CREATIVE_MODE_TAB, id);
        CreativeModeTab tab = supplier.get();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, key, tab);
        return new TabInfo(key, tab);
    }

    public static ResourceKey<CreativeModeTab> getPalettesTabKey() {
        return PALETTES_TAB.key();
    }

    public static boolean isInCreativeTab(RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        return Railways.registrate().isInCreativeTab(entry, tab);
    }
}
