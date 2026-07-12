package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.api.bogeymenu.v0.BogeyMenuManager;
import com.railwayteam.railways.api.bogeymenu.v0.entry.CategoryEntry;
import com.railwayteam.railways.content.custom_bogeys.special.monobogey.InvisibleMonoBogeyBlock;
import com.railwayteam.railways.impl.bogeymenu.v0.BogeyMenuManagerImpl;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CRBogeyStyles {
	public static final String SINGLEAXLE_CYCLE_GROUP = "singleaxles";
	public static final String DOUBLEAXLE_CYCLE_GROUP = "doubleaxles";
	public static final String TRIPLEAXLE_CYCLE_GROUP = "tripleaxles";
	public static final String QUADRUPLEAXLE_CYCLE_GROUP = "quadrupleaxles";
	public static final String QUINTUPLEAXLE_CYCLE_GROUP = "quintupleaxles";
	public static final String SEXTUPLEAXLE_CYCLE_GROUP = "sextupleaxles";

	private static final Map<Pair<BogeyStyle, Identifier>, BogeyStyle> STYLES_FOR_GAUGES = new HashMap<>();
	private static final Map<BogeyStyle, BogeyStyle> STYLES_TO_STANDARD_GAUGE = new HashMap<>();
	private static final Map<BogeyStyle, List<BogeyStyle>> SUB_STYLES = new HashMap<>();

	public static BogeyStyle SINGLEAXLE = AllBogeyStyles.STANDARD;
	public static BogeyStyle LEAFSPRING = AllBogeyStyles.STANDARD;
	public static BogeyStyle COILSPRING = AllBogeyStyles.STANDARD;
	public static BogeyStyle FREIGHT = AllBogeyStyles.STANDARD;
	public static BogeyStyle ARCHBAR = AllBogeyStyles.STANDARD;
	public static BogeyStyle PASSENGER = AllBogeyStyles.STANDARD;
	public static BogeyStyle MODERN = AllBogeyStyles.STANDARD;
	public static BogeyStyle BLOMBERG = AllBogeyStyles.STANDARD;
	public static BogeyStyle Y25 = AllBogeyStyles.STANDARD;
	public static BogeyStyle HEAVYWEIGHT = AllBogeyStyles.STANDARD;
	public static BogeyStyle RADIAL = AllBogeyStyles.STANDARD;
	public static BogeyStyle HANDCAR = AllBogeyStyles.STANDARD;
	public static BogeyStyle INVISIBLE = AllBogeyStyles.STANDARD;
	public static BogeyStyle MONOBOGEY = AllBogeyStyles.STANDARD;
	public static BogeyStyle INVISIBLE_MONOBOGEY = AllBogeyStyles.STANDARD;
	public static BogeyStyle WIDE_DEFAULT = AllBogeyStyles.STANDARD;
	public static BogeyStyle WIDE_COMICALLY_LARGE = AllBogeyStyles.STANDARD;
	public static BogeyStyle NARROW_DEFAULT = AllBogeyStyles.STANDARD;
	public static BogeyStyle NARROW_DOUBLE_SCOTCH = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_STANDARD = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_SINGLE_WHEEL = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_2_0_2_TRAILING = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_4_0_4_TRAILING = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_6_0_6_TRAILING = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_6_0_6_TENDER = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_8_0_8_TENDER = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_10_0_10_TENDER = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_TRIPLE_WHEEL = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_QUADRUPLE_WHEEL = AllBogeyStyles.STANDARD;
	public static BogeyStyle MEDIUM_QUINTUPLE_WHEEL = AllBogeyStyles.STANDARD;
	public static BogeyStyle LARGE_CREATE_STYLED_0_4_0 = AllBogeyStyles.STANDARD;
	public static BogeyStyle LARGE_CREATE_STYLED_0_6_0 = AllBogeyStyles.STANDARD;
	public static BogeyStyle LARGE_CREATE_STYLED_0_8_0 = AllBogeyStyles.STANDARD;
	public static BogeyStyle LARGE_CREATE_STYLED_0_10_0 = AllBogeyStyles.STANDARD;
	public static BogeyStyle LARGE_CREATE_STYLED_0_12_0 = AllBogeyStyles.STANDARD;

	private static boolean registered;

	public static void register() {
		if (registered)
			return;
		registered = true;

		SINGLEAXLE = create("singleaxle", SINGLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.SINGLEAXLE_BOGEY.get()).build();
		LEAFSPRING = create("leafspring", SINGLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.SINGLEAXLE_BOGEY.get()).build();
		COILSPRING = create("coilspring", SINGLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.SINGLEAXLE_BOGEY.get()).build();

		FREIGHT = create("freight", DOUBLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.LARGE_PLATFORM_DOUBLEAXLE_BOGEY.get()).build();
		ARCHBAR = create("archbar", DOUBLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.LARGE_PLATFORM_DOUBLEAXLE_BOGEY.get()).build();
		PASSENGER = create("passenger", DOUBLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.DOUBLEAXLE_BOGEY.get()).build();
		MODERN = create("modern", DOUBLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.DOUBLEAXLE_BOGEY.get()).build();
		BLOMBERG = create("blomberg", DOUBLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.DOUBLEAXLE_BOGEY.get()).build();
		Y25 = create("y25", DOUBLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.LARGE_PLATFORM_DOUBLEAXLE_BOGEY.get()).build();

		HEAVYWEIGHT = create("heavyweight", TRIPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.TRIPLEAXLE_BOGEY.get()).build();
		RADIAL = create("radial", TRIPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.TRIPLEAXLE_BOGEY.get()).build();
		HANDCAR = create("handcar", "handcar_cycle_group").size(AllBogeySizes.SMALL, CRBlocks.HANDCAR.get()).build();
		INVISIBLE = create("invisible", AllBogeyStyles.STANDARD_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.INVISIBLE_BOGEY.get()).build();
		MONOBOGEY = create("monobogey", "monobogey").size(AllBogeySizes.SMALL, CRBlocks.MONO_BOGEY.get()).build();
		INVISIBLE_MONOBOGEY = create("invisible_monobogey", "monobogey").size(AllBogeySizes.SMALL, CRBlocks.INVISIBLE_MONO_BOGEY.get()).build();

		WIDE_DEFAULT = create("wide_default", AllBogeyStyles.STANDARD_CYCLE_GROUP)
			.size(AllBogeySizes.SMALL, CRBlocks.WIDE_DOUBLEAXLE_BOGEY.get())
			.size(AllBogeySizes.LARGE, CRBlocks.WIDE_SCOTCH_BOGEY.get())
			.build();
		WIDE_COMICALLY_LARGE = create("wide_comically_large", AllBogeyStyles.STANDARD_CYCLE_GROUP)
			.size(AllBogeySizes.LARGE, CRBlocks.WIDE_COMICALLY_LARGE_BOGEY.get())
			.build();
		NARROW_DEFAULT = create("narrow_default", AllBogeyStyles.STANDARD_CYCLE_GROUP)
			.size(AllBogeySizes.SMALL, CRBlocks.NARROW_SMALL_BOGEY.get())
			.size(AllBogeySizes.LARGE, CRBlocks.NARROW_SCOTCH_BOGEY.get())
			.build();
		NARROW_DOUBLE_SCOTCH = create("narrow_double_scotch", AllBogeyStyles.STANDARD_CYCLE_GROUP)
			.size(AllBogeySizes.LARGE, CRBlocks.NARROW_DOUBLE_SCOTCH_BOGEY.get())
			.build();

		MEDIUM_STANDARD = create("medium_standard", DOUBLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_BOGEY.get()).build();
		MEDIUM_SINGLE_WHEEL = create("medium_single_wheel", SINGLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_BOGEY.get()).build();
		MEDIUM_2_0_2_TRAILING = create("medium_2_0_2_trailing", SINGLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_2_0_2_TRAILING.get()).build();
		MEDIUM_4_0_4_TRAILING = create("medium_4_0_4_trailing", DOUBLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_4_0_4_TRAILING.get()).build();
		MEDIUM_TRIPLE_WHEEL = create("medium_triple_wheel", TRIPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_TRIPLE_WHEEL.get()).build();
		MEDIUM_6_0_6_TRAILING = create("medium_6_0_6_trailing", TRIPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_TRIPLE_WHEEL.get()).build();
		MEDIUM_6_0_6_TENDER = create("medium_6_0_6_tender", TRIPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_TRIPLE_WHEEL.get()).build();
		MEDIUM_QUADRUPLE_WHEEL = create("medium_quadruple_wheel", QUADRUPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_QUADRUPLE_WHEEL.get()).build();
		MEDIUM_8_0_8_TENDER = create("medium_8_0_8_tender", QUADRUPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_QUADRUPLE_WHEEL.get()).build();
		MEDIUM_QUINTUPLE_WHEEL = create("medium_quintuple_wheel", QUINTUPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_QUINTUPLE_WHEEL.get()).build();
		MEDIUM_10_0_10_TENDER = create("medium_10_0_10_tender", QUINTUPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.SMALL, CRBlocks.MEDIUM_QUINTUPLE_WHEEL.get()).build();

		LARGE_CREATE_STYLED_0_4_0 = create("large_create_style_0_4_0", DOUBLEAXLE_CYCLE_GROUP).size(AllBogeySizes.LARGE, CRBlocks.LARGE_CREATE_STYLE_0_4_0.get()).build();
		LARGE_CREATE_STYLED_0_6_0 = create("large_create_style_0_6_0", TRIPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.LARGE, CRBlocks.LARGE_CREATE_STYLE_0_6_0.get()).build();
		LARGE_CREATE_STYLED_0_8_0 = create("large_create_style_0_8_0", QUADRUPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.LARGE, CRBlocks.LARGE_CREATE_STYLE_0_8_0.get()).build();
		LARGE_CREATE_STYLED_0_10_0 = create("large_create_style_0_10_0", QUINTUPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.LARGE, CRBlocks.LARGE_CREATE_STYLE_0_10_0.get()).build();
		LARGE_CREATE_STYLED_0_12_0 = create("large_create_style_0_12_0", SEXTUPLEAXLE_CYCLE_GROUP).size(AllBogeySizes.LARGE, CRBlocks.LARGE_CREATE_STYLE_0_12_0.get()).build();

		map(AllBogeyStyles.STANDARD, CRTrackMaterials.CRTrackType.WIDE_GAUGE, WIDE_DEFAULT);
		map(AllBogeyStyles.STANDARD, CRTrackMaterials.CRTrackType.NARROW_GAUGE, NARROW_DEFAULT);
		mapReverse(WIDE_DEFAULT, AllBogeyStyles.STANDARD);
		mapReverse(WIDE_COMICALLY_LARGE, AllBogeyStyles.STANDARD);
		mapReverse(NARROW_DEFAULT, AllBogeyStyles.STANDARD);
		mapReverse(NARROW_DOUBLE_SCOTCH, AllBogeyStyles.STANDARD);
		listUnder(WIDE_DEFAULT, AllBogeyStyles.STANDARD);
		listUnder(NARROW_DEFAULT, AllBogeyStyles.STANDARD);

		registerMenuEntries();
	}

	private static BogeyStyle.Builder create(String name, String cycleGroup) {
		return create(name, Railways.asResource(cycleGroup));
	}

	private static BogeyStyle.Builder create(String name, Identifier cycleGroup) {
		return new BogeyStyle.Builder(Railways.asResource(name), cycleGroup)
			.displayName(Component.translatable("railways.bogeys.styles." + name));
	}

	private static void registerMenuEntries() {
		CategoryEntry standard = registerCategory("create", "standard");
		CategoryEntry singleAxle = registerCategory(SINGLEAXLE_CYCLE_GROUP);
		CategoryEntry doubleAxle = registerCategory(DOUBLEAXLE_CYCLE_GROUP);
		CategoryEntry tripleAxle = registerCategory(TRIPLEAXLE_CYCLE_GROUP);
		CategoryEntry quadrupleAxle = registerCategory(QUADRUPLEAXLE_CYCLE_GROUP);
		CategoryEntry quintupleAxle = registerCategory(QUINTUPLEAXLE_CYCLE_GROUP);
		CategoryEntry sextupleAxle = registerCategory(SEXTUPLEAXLE_CYCLE_GROUP);

		addToCategory(standard, INVISIBLE);
		addToCategory(standard, WIDE_COMICALLY_LARGE, 17);
		addToCategory(standard, AllBogeyStyles.STANDARD);
		addToCategory(standard, NARROW_DOUBLE_SCOTCH);

		addToCategory(singleAxle, SINGLEAXLE);
		addToCategory(singleAxle, COILSPRING);
		addToCategory(singleAxle, LEAFSPRING);
		addToCategory(singleAxle, MEDIUM_SINGLE_WHEEL);
		addToCategory(singleAxle, MEDIUM_2_0_2_TRAILING);

		addToCategory(doubleAxle, MODERN);
		addToCategory(doubleAxle, BLOMBERG);
		addToCategory(doubleAxle, Y25);
		addToCategory(doubleAxle, FREIGHT);
		addToCategory(doubleAxle, PASSENGER);
		addToCategory(doubleAxle, ARCHBAR);
		addToCategory(doubleAxle, MEDIUM_STANDARD);
		addToCategory(doubleAxle, MEDIUM_4_0_4_TRAILING);
		addToCategory(doubleAxle, LARGE_CREATE_STYLED_0_4_0);

		addToCategory(tripleAxle, HEAVYWEIGHT, 20);
		addToCategory(tripleAxle, RADIAL, 20);
		addToCategory(tripleAxle, MEDIUM_6_0_6_TRAILING, 20);
		addToCategory(tripleAxle, MEDIUM_6_0_6_TENDER, 20);
		addToCategory(tripleAxle, LARGE_CREATE_STYLED_0_6_0, 20);

		addToCategory(quadrupleAxle, MEDIUM_QUADRUPLE_WHEEL, 19);
		addToCategory(quadrupleAxle, MEDIUM_8_0_8_TENDER, 19);
		addToCategory(quadrupleAxle, LARGE_CREATE_STYLED_0_8_0, 17);

		addToCategory(quintupleAxle, MEDIUM_QUINTUPLE_WHEEL, 17);
		addToCategory(quintupleAxle, MEDIUM_10_0_10_TENDER, 17);
		addToCategory(quintupleAxle, LARGE_CREATE_STYLED_0_10_0, 15);

		addToCategory(sextupleAxle, LARGE_CREATE_STYLED_0_12_0, 13);

		BogeyMenuManager.INSTANCE.setScalesForBogeySizes(WIDE_DEFAULT, AllBogeySizes.SMALL, 20);
	}

	private static CategoryEntry registerCategory(String name) {
		return registerCategory(Railways.MOD_ID, name);
	}

	private static CategoryEntry registerCategory(String modId, String name) {
		return BogeyMenuManager.INSTANCE.registerCategory(
			Component.translatable(modId + ".gui.bogey_menu.category." + name),
			Identifier.fromNamespaceAndPath(modId, "bogey_menu/category/" + name)
		);
	}

	private static void addToCategory(CategoryEntry category, BogeyStyle style) {
		addToCategory(category, style, BogeyMenuManagerImpl.defaultScale);
	}

	private static void addToCategory(CategoryEntry category, BogeyStyle style, float scale) {
		String path = style == AllBogeyStyles.STANDARD ? "default" : style.id.getPath();
		Identifier icon = Railways.asResource("textures/gui/bogey_icons/" + path + "_icon.png");
		BogeyMenuManager.INSTANCE.addToCategory(category, style, icon, scale);
	}

	public static void map(BogeyStyle from, Identifier toType, BogeyStyle toStyle) {
		STYLES_FOR_GAUGES.put(Pair.of(from, toType), toStyle);
	}

	public static void mapReverse(BogeyStyle gaugeStyle, BogeyStyle standardStyle) {
		STYLES_TO_STANDARD_GAUGE.put(gaugeStyle, standardStyle);
	}

	public static List<BogeyStyle> getSubStyles(BogeyStyle style) {
		return SUB_STYLES.getOrDefault(style, List.of());
	}

	public static void listUnder(BogeyStyle target, BogeyStyle parent) {
		SUB_STYLES.computeIfAbsent(parent, style -> new ArrayList<>()).add(target);
	}

	public static boolean styleFitsTrack(BogeyStyle style, Identifier trackType) {
		for (BogeySize size : style.validSizes()) {
			if (blockFitsTrack(style.getBlockForSize(size), style, trackType))
				return true;
		}
		return false;
	}

	public static boolean blockFitsTrack(AbstractBogeyBlock<?> bogeyBlock, BogeyStyle style, Identifier trackType) {
		if (trackType.equals(CRTrackMaterials.CRTrackType.UNIVERSAL))
			return true;

		boolean validType = bogeyBlock.getValidPathfindingTypes(style).contains(trackType)
			|| bogeyBlock.getValidPathfindingTypes(style).contains(CRTrackMaterials.CRTrackType.UNIVERSAL);
		boolean monoRail = trackType.equals(CRTrackMaterials.CRTrackType.MONORAIL);
		return validType && ((!monoRail) ^ bogeyBlock instanceof InvisibleMonoBogeyBlock);
	}

	public static List<BogeyStyle> filterStylesForTrack(Collection<BogeyStyle> styles, Identifier trackType) {
		return styles.stream()
			.filter(style -> styleFitsTrack(style, trackType))
			.toList();
	}

	public static Optional<BogeyStyle> getMapped(BogeyStyle style, Identifier trackType, boolean fallback) {
		if (styleFitsTrack(style, trackType))
			return Optional.of(style);

		BogeyStyle mapped = STYLES_FOR_GAUGES.get(Pair.of(style, trackType));
		if (mapped != null)
			return Optional.of(mapped);

		if (trackType.equals(CRTrackMaterials.CRTrackType.STANDARD)) {
			BogeyStyle standard = STYLES_TO_STANDARD_GAUGE.get(style);
			if (standard != null)
				return Optional.of(standard);
			// Match the original menu implementation: a custom standard-gauge style
			// should remain selected when it has no explicit reverse mapping. Forcing
			// a registry fallback here can select any compatible style, including the
			// invisible bogey, because BOGEY_STYLES is backed by a HashMap.
			return Optional.empty();
		} else {
			BogeyStyle standard = STYLES_TO_STANDARD_GAUGE.get(style);
			if (standard != null) {
				BogeyStyle gaugeStyle = STYLES_FOR_GAUGES.get(Pair.of(standard, trackType));
				if (gaugeStyle != null)
					return Optional.of(gaugeStyle);
			}
		}

		if (!fallback)
			return Optional.empty();
		return AllBogeyStyles.BOGEY_STYLES.values().stream()
			.filter(candidate -> styleFitsTrack(candidate, trackType))
			.findFirst();
	}

	public static Optional<ResolvedBogey> resolveForTrack(BogeyStyle selectedStyle, BogeySize preferredSize,
														 Identifier trackType, boolean fallback) {
		Optional<BogeyStyle> mappedStyle = trackType.equals(CRTrackMaterials.CRTrackType.UNIVERSAL)
			? Optional.of(selectedStyle)
			: getMapped(selectedStyle, trackType, fallback);
		if (mappedStyle.isEmpty())
			return Optional.empty();

		BogeyStyle style = mappedStyle.get();
		BogeySize size = preferredSize != null
			? preferredSize
			: AllBogeySizes.allSortedIncreasing().getFirst();
		for (int attempts = 0; attempts < AllBogeySizes.allSortedIncreasing().size(); attempts++) {
			if (style.validSizes().contains(size)) {
				AbstractBogeyBlock<?> block = style.getBlockForSize(size);
				if (blockFitsTrack(block, style, trackType))
					return Optional.of(new ResolvedBogey(style, size, block));
			}
			size = size.nextBySize();
		}
		return Optional.empty();
	}

	public record ResolvedBogey(BogeyStyle style, BogeySize size, AbstractBogeyBlock<?> block) {
	}
}
