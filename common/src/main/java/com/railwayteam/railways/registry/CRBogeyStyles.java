package com.railwayteam.railways.registry;

import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public class CRBogeyStyles {
	public static final BogeyStyle SINGLEAXLE = AllBogeyStyles.STANDARD;
	public static final BogeyStyle PASSENGER = AllBogeyStyles.STANDARD;
	public static final BogeyStyle HEAVYWEIGHT = AllBogeyStyles.STANDARD;
	public static final BogeyStyle HANDCAR = AllBogeyStyles.STANDARD;
	public static final BogeyStyle INVISIBLE = AllBogeyStyles.STANDARD;
	public static final BogeyStyle MONOBOGEY = AllBogeyStyles.STANDARD;
	public static final BogeyStyle INVISIBLE_MONOBOGEY = AllBogeyStyles.STANDARD;
	public static final BogeyStyle WIDE_DEFAULT = AllBogeyStyles.STANDARD;
	public static final BogeyStyle WIDE_COMICALLY_LARGE = AllBogeyStyles.STANDARD;
	public static final BogeyStyle NARROW_DEFAULT = AllBogeyStyles.STANDARD;
	public static final BogeyStyle NARROW_DOUBLE_SCOTCH = AllBogeyStyles.STANDARD;
	public static final BogeyStyle MEDIUM_STANDARD = AllBogeyStyles.STANDARD;
	public static final BogeyStyle MEDIUM_2_0_2_TRAILING = AllBogeyStyles.STANDARD;
	public static final BogeyStyle MEDIUM_4_0_4_TRAILING = AllBogeyStyles.STANDARD;
	public static final BogeyStyle MEDIUM_TRIPLE_WHEEL = AllBogeyStyles.STANDARD;
	public static final BogeyStyle MEDIUM_QUADRUPLE_WHEEL = AllBogeyStyles.STANDARD;
	public static final BogeyStyle MEDIUM_QUINTUPLE_WHEEL = AllBogeyStyles.STANDARD;
	public static final BogeyStyle LARGE_CREATE_STYLED_0_4_0 = AllBogeyStyles.STANDARD;
	public static final BogeyStyle LARGE_CREATE_STYLED_0_6_0 = AllBogeyStyles.STANDARD;
	public static final BogeyStyle LARGE_CREATE_STYLED_0_8_0 = AllBogeyStyles.STANDARD;
	public static final BogeyStyle LARGE_CREATE_STYLED_0_10_0 = AllBogeyStyles.STANDARD;
	public static final BogeyStyle LARGE_CREATE_STYLED_0_12_0 = AllBogeyStyles.STANDARD;

	public static void register() {
	}

	public static List<BogeyStyle> getSubStyles(BogeyStyle style) {
		return List.of();
	}

	public static boolean styleFitsTrack(BogeyStyle style, Identifier trackType) {
		return true;
	}

	public static Optional<BogeyStyle> getMapped(BogeyStyle style, Identifier trackType, boolean fallback) {
		return Optional.ofNullable(style);
	}
}
