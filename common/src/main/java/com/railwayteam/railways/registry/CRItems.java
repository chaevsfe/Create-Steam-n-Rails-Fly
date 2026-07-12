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

import com.google.common.collect.ImmutableList;
import com.railwayteam.railways.ModSetup;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.base.data.BuilderTransformers;
import com.railwayteam.railways.content.conductor.ConductorCapItem;
import com.railwayteam.railways.content.conductor.remote_lens.RemoteLensItem;
import com.railwayteam.railways.content.minecarts.MinecartJukebox;
import com.railwayteam.railways.content.minecarts.MinecartWorkbench;
import com.railwayteam.railways.content.palettes.painting.EmptyPaintPitcherItem;
import com.railwayteam.railways.content.palettes.painting.PaintBrushItem;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.registry.CRPalettes.DyedOnlyPalettesColorList;
import com.railwayteam.railways.util.TextUtils;
import com.zurrtum.create.AllTags;
import com.zurrtum.create.content.processing.sequenced.SequencedAssemblyItem;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static com.railwayteam.railways.util.TextUtils.snakeCaseToTitleCase;

public class CRItems {
    private static final CreateRegistrate REGISTRATE = Railways.registrate();

    public static final TagKey<Item> CONDUCTOR_CAPS = CRTags.AllItemTags.CONDUCTOR_CAPS.tag;

    public static TagKey<Item> makeItemTag(String mod, String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(mod, path));
    }

    private static ItemBuilder<? extends Item, ?> makeMinecart(String name, Supplier<? extends EntityType<? extends AbstractMinecart>> type) {
        return REGISTRATE.item(name, (props) -> new MinecartItem(type.get(), props));
    }

    public static Item woolByColor(DyeColor color) {
        return Items.WOOL.pick(color);
    }

    public static final ItemEntry<? extends Item> ITEM_BENCHCART = makeMinecart("benchcart", CREntities.CART_BLOCK)
        .lang("Minecart with Workbench")
        .register();
    public static final ItemEntry<? extends Item> ITEM_JUKEBOXCART = makeMinecart("jukeboxcart", CREntities.CART_JUKEBOX)
        .lang("Minecart with Jukebox")
        .register();

    public static final ItemEntry<? extends RemoteLensItem> REMOTE_LENS = REGISTRATE.item("remote_lens", RemoteLensItem::new)
        .lang("Remote Lens")
        .register();

    public static final EnumMap<DyeColor, ItemEntry<ConductorCapItem>> ITEM_CONDUCTOR_CAP = new EnumMap<>(DyeColor.class);
    public static final EnumMap<DyeColor, ItemEntry<SequencedAssemblyItem>> ITEM_INCOMPLETE_CONDUCTOR_CAP = new EnumMap<>(DyeColor.class);

    public static final Map<TrackMaterial, ItemEntry<SequencedAssemblyItem>> ITEM_INCOMPLETE_TRACK = new HashMap<>();

    static {
        for (DyeColor color : DyeColor.values()) {
            String colorName = TextUtils.titleCaseConversion(color.getName().replace("_", " "));
            String colorReg  = color.getName().toLowerCase(Locale.ROOT);
            ITEM_INCOMPLETE_CONDUCTOR_CAP.put(color, REGISTRATE.item(colorReg + "_incomplete_conductor_cap", SequencedAssemblyItem::new)
                .lang("Incomplete " + colorName + " Conductor's Cap")
                .register());
            ITEM_CONDUCTOR_CAP.put(color, REGISTRATE.item(colorReg + "_conductor_cap", p -> ConductorCapItem.create(p, color))
                .lang(colorName + " Conductor's Cap")
                .tag(CONDUCTOR_CAPS)
                .properties(p -> p.stacksTo(1))
                .register());
        }

        for (TrackMaterial material : CRTrackMaterials.allFromMod(Railways.MOD_ID)) {
            ITEM_INCOMPLETE_TRACK.put(material, REGISTRATE.item("track_incomplete_" + material.getId().getPath(), SequencedAssemblyItem::new)
                .lang("Incomplete " + CRTrackMaterials.langName(material) + " Track")
                .register());
        }
    }

    static {
        ModSetup.usePalettesTab();
    }

    public static final ItemEntry<? extends PaintBrushItem> PAINT_BRUSH = REGISTRATE.item("paint_brush", PaintBrushItem::new)
        .properties(p -> p.durability(250))
        .lang("Paint Brush")
        .register();

    public static final ItemEntry<? extends Item> EMPTY_PAINT_PITCHER = REGISTRATE.item("empty_paint_pitcher", EmptyPaintPitcherItem::create)
        .lang("Empty Paint Pitcher")
        .tag(AllTags.AllItemTags.UPRIGHT_ON_BELT.tag)
        .register();

    public static final ItemEntry<? extends PaintPitcherItem> SANDY_PITCHER = REGISTRATE.item("sandy_paint_pitcher", p -> PaintPitcherItem.create(p, null))
        .transform(BuilderTransformers.paintPitcher())
        .properties(p -> p.stacksTo(1))
        .tag(AllTags.AllItemTags.UPRIGHT_ON_BELT.tag)
        .tag(CRTags.AllItemTags.FILLED_PAINT_PITCHERS.tag)
        .lang("Sandy Paint Pitcher")
        .register();

    public static final DyedOnlyPalettesColorList<ItemEntry<? extends PaintPitcherItem>> PAINT_PITCHERS = new DyedOnlyPalettesColorList<>((color) -> {
        String colorReg  = color.getSerializedName();
        String colorName = snakeCaseToTitleCase(colorReg);
        return REGISTRATE.item(colorReg + "_paint_pitcher", p -> PaintPitcherItem.create(p, color))
            .transform(BuilderTransformers.paintPitcher())
            .properties(p -> p.stacksTo(1))
            .tag(AllTags.AllItemTags.UPRIGHT_ON_BELT.tag)
            .tag(CRTags.AllItemTags.FILLED_PAINT_PITCHERS.tag)
            .lang(colorName + " Paint Pitcher")
            .register();
    });

    public static final List<ItemEntry<? extends PaintPitcherItem>> FILLED_PITCHERS = ImmutableList.<ItemEntry<? extends PaintPitcherItem>>builder()
        .addAll(PAINT_PITCHERS)
        .add(SANDY_PITCHER)
        .build();

    static {
        ModSetup.useBaseTab();
    }

    private static ItemEntry<SequencedAssemblyItem> sequencedIngredient(String name) {
        return REGISTRATE.item(name, SequencedAssemblyItem::new)
            .register();
    }

    @SuppressWarnings("EmptyMethod")
    public static void register() {}
}
