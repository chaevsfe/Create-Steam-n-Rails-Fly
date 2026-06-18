package com.railwayteam.railways.content.bogey_menu.handler;

import com.railwayteam.railways.api.bogeymenu.v0.entry.BogeyEntry;
import com.zurrtum.create.client.foundation.gui.widget.Indicator;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.catnip.data.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BogeyMenuHandlerClient {
	private static final List<BogeyStyle> FAVORITES = new ArrayList<>();

	public static void addFavorite(BogeyStyle style) {
		if (!FAVORITES.contains(style))
			FAVORITES.add(style);
	}

	public static void removeFavorite(BogeyStyle style) {
		FAVORITES.remove(style);
	}

	public static void toggleFavorite(BogeyStyle style) {
		if (FAVORITES.contains(style))
			removeFavorite(style);
		else
			addFavorite(style);
	}

	public static boolean isFavorited(BogeyStyle style) {
		return FAVORITES.contains(style);
	}

	public static List<BogeyStyle> getFavorites() {
		return FAVORITES;
	}

	public static @Nullable BogeySize getSize(BogeyStyle style) {
		for (BogeySize size : style.validSizes())
			return size;
		return null;
	}

	public static List<Pair<BogeyStyle, BogeySize>> getRenderCycle(BogeyStyle style) {
		BogeySize size = getSize(style);
		return size == null ? List.of() : List.of(Pair.of(style, size));
	}

	public static Indicator.State[] getTrackCompat(BogeyEntry bogeyEntry) {
		return new Indicator.State[] { Indicator.State.GREEN, Indicator.State.GREEN, Indicator.State.GREEN };
	}
}
