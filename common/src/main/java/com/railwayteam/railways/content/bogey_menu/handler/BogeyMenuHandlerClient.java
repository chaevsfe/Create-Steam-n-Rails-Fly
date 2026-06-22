package com.railwayteam.railways.content.bogey_menu.handler;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.api.bogeymenu.v0.entry.BogeyEntry;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.foundation.gui.widget.Indicator;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BogeyMenuHandlerClient {
	private static final Map<BogeyStyle, List<Pair<BogeyStyle, BogeySize>>> CACHED_RENDER_CYCLES = new HashMap<>();
	private static final Map<BogeyEntry, Indicator.State[]> CACHED_COMPATS = new HashMap<>();

	@Nullable
	private static List<BogeyStyle> favorites = null;
	@Nullable
	private static BogeyStyle lastSelected = null;

	public static void setLastSelected(BogeyStyle style) {
		lastSelected = style;
	}

	public static java.util.Optional<BogeyStyle> getLastSelected() {
		return java.util.Optional.ofNullable(lastSelected);
	}

	public static void addFavorite(BogeyStyle style) {
		if (favorites == null)
			loadFavorites();
		if (favorites.contains(style))
			return;
		favorites.add(style);
		saveFavorites();
	}

	public static void removeFavorite(BogeyStyle style) {
		if (favorites == null)
			loadFavorites();
		if (!favorites.contains(style))
			return;
		favorites.remove(style);
		saveFavorites();
	}

	public static void toggleFavorite(BogeyStyle style) {
		if (favorites == null)
			loadFavorites();
		if (favorites.contains(style))
			removeFavorite(style);
		else
			addFavorite(style);
	}

	public static boolean isFavorited(BogeyStyle style) {
		if (favorites == null)
			loadFavorites();
		return favorites.contains(style);
	}

	public static List<BogeyStyle> getFavorites() {
		if (favorites == null)
			loadFavorites();
		return favorites;
	}

	private static void optimizeFavorites() {
		List<BogeyStyle> newFavorites = new ArrayList<>();
		for (BogeyStyle style : getFavorites())
			if (!newFavorites.contains(style))
				newFavorites.add(style);
		favorites = newFavorites;
		saveFavorites();
	}

	private static void loadFavorites() {
		favorites = new ArrayList<>();
		try {
			File file = new File(Minecraft.getInstance().gameDirectory, "snr_favorite_styles.nbt");
			CompoundTag tag = NbtIo.read(file.toPath());
			if (tag == null)
				return;

			ListTag favoritesList = tag.getList("Favorites").orElse(null);
			if (favoritesList == null)
				return;

			favorites.clear();
			for (Tag favoriteTag : favoritesList) {
				if (!(favoriteTag instanceof StringTag stringTag))
					continue;
				Identifier loc = Identifier.tryParse(stringTag.value());
				if (loc == null)
					continue;
				BogeyStyle style = AllBogeyStyles.BOGEY_STYLES.get(loc);
				if (style != null)
					favorites.add(style);
			}
			optimizeFavorites();
		} catch (Exception e) {
			Railways.LOGGER.error("Failed to load favorite styles", e);
		}
	}

	private static void saveFavorites() {
		if (favorites == null)
			return;
		try {
			CompoundTag tag = new CompoundTag();
			ListTag listTag = new ListTag();
			for (BogeyStyle style : favorites)
				listTag.add(StringTag.valueOf(style.id.toString()));
			tag.put("Favorites", listTag);
			NbtIo.write(tag, new File(Minecraft.getInstance().gameDirectory, "snr_favorite_styles.nbt").toPath());
		} catch (Exception e) {
			Railways.LOGGER.error("Failed to save favorite styles", e);
		}
	}

	public static @Nullable BogeySize getSize(BogeyStyle style) {
		for (BogeySize size : style.validSizes())
			return size;
		return null;
	}

	public static List<Pair<BogeyStyle, BogeySize>> getRenderCycle(BogeyStyle style) {
		return CACHED_RENDER_CYCLES.computeIfAbsent(style, s -> {
			List<Pair<BogeyStyle, BogeySize>> cycle = new ArrayList<>();
			for (BogeySize size : style.validSizes())
				cycle.add(Pair.of(style, size));

			for (BogeyStyle subStyle : CRBogeyStyles.getSubStyles(style))
				for (BogeySize size : subStyle.validSizes())
					cycle.add(Pair.of(subStyle, size));

			return cycle;
		});
	}

	public static Indicator.State[] getTrackCompat(BogeyEntry bogeyEntry) {
		return CACHED_COMPATS.computeIfAbsent(bogeyEntry, entry -> new Indicator.State[] {
			styleFits(entry, CRTrackMaterials.CRTrackType.NARROW_GAUGE),
			styleFits(entry, CRTrackMaterials.CRTrackType.STANDARD),
			styleFits(entry, CRTrackMaterials.CRTrackType.WIDE_GAUGE)
		});
	}

	private static Indicator.State styleFits(BogeyEntry bogeyEntry, Identifier trackType) {
		if (CRBogeyStyles.styleFitsTrack(bogeyEntry.bogeyStyle(), trackType))
			return Indicator.State.GREEN;

		for (BogeyStyle subStyle : CRBogeyStyles.getSubStyles(bogeyEntry.bogeyStyle()))
			if (CRBogeyStyles.styleFitsTrack(subStyle, trackType))
				return Indicator.State.GREEN;

		return Indicator.State.RED;
	}
}
