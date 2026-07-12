/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.custom_bogeys;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.bogey_menu.handler.BogeyMenuHandlerServer;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.UUID;

/** Development-only regression matrix for bogey gauge selection and placement. */
public final class BogeyGaugeRuntimeChecks {
	private static final UUID PROBE_PLAYER = UUID.fromString("bd33fe3e-0f8b-4b0c-a215-f69ca776fcad");
	private static boolean registered;
	private static boolean checked;

	private BogeyGaugeRuntimeChecks() {
	}

	public static void register() {
		if (registered || !Utils.isDevEnv())
			return;
		registered = true;
		ServerLifecycleEvents.SERVER_STARTED.register(BogeyGaugeRuntimeChecks::run);
	}

	private static void run(MinecraftServer server) {
		if (checked)
			return;
		checked = true;

		checkMappings();
		checkWrenchCycleFilters();
		checkContextualStyleCycle(server.overworld());
		checkTrackAnchors(server);
		Railways.LOGGER.info(
			"Bogey gauge mapping, wrench-cycle filtering, and TrackBlock anchor runtime checks passed"
		);
	}

	private static void checkMappings() {
		BogeyStyle[] selectedStyles = {
			AllBogeyStyles.STANDARD,
			CRBogeyStyles.WIDE_DEFAULT,
			CRBogeyStyles.NARROW_DEFAULT
		};
		Identifier[] trackTypes = {
			CRTrackMaterials.CRTrackType.STANDARD,
			CRTrackMaterials.CRTrackType.WIDE_GAUGE,
			CRTrackMaterials.CRTrackType.NARROW_GAUGE
		};
		BogeyStyle[] expectedStyles = {
			AllBogeyStyles.STANDARD,
			CRBogeyStyles.WIDE_DEFAULT,
			CRBogeyStyles.NARROW_DEFAULT
		};

		for (BogeyStyle selected : selectedStyles) {
			for (int i = 0; i < trackTypes.length; i++) {
				BogeyStyle resolved = resolve(selected, trackTypes[i]);
				require(resolved == expectedStyles[i],
					"default gauge mapping returned an unexpected style for " + trackTypes[i]);
			}
		}

		require(resolve(CRBogeyStyles.WIDE_COMICALLY_LARGE, CRTrackMaterials.CRTrackType.WIDE_GAUGE)
			== CRBogeyStyles.WIDE_COMICALLY_LARGE, "wide same-gauge mapping changed the selected style");
		require(resolve(CRBogeyStyles.NARROW_DOUBLE_SCOTCH, CRTrackMaterials.CRTrackType.NARROW_GAUGE)
			== CRBogeyStyles.NARROW_DOUBLE_SCOTCH, "narrow same-gauge mapping changed the selected style");
	}

	private static void checkWrenchCycleFilters() {
		var cycleGroup = AllBogeyStyles.STANDARD.getCycleGroup().values();
		List<BogeyStyle> standard = CRBogeyStyles.filterStylesForTrack(
			cycleGroup, CRTrackMaterials.CRTrackType.STANDARD
		);
		List<BogeyStyle> wide = CRBogeyStyles.filterStylesForTrack(
			cycleGroup, CRTrackMaterials.CRTrackType.WIDE_GAUGE
		);
		List<BogeyStyle> narrow = CRBogeyStyles.filterStylesForTrack(
			cycleGroup, CRTrackMaterials.CRTrackType.NARROW_GAUGE
		);

		require(standard.contains(AllBogeyStyles.STANDARD), "standard cycle lost the standard bogey");
		require(!standard.contains(CRBogeyStyles.WIDE_DEFAULT)
			&& !standard.contains(CRBogeyStyles.NARROW_DEFAULT),
			"standard cycle contains a non-standard gauge bogey");
		require(wide.contains(CRBogeyStyles.WIDE_DEFAULT)
			&& wide.contains(CRBogeyStyles.WIDE_COMICALLY_LARGE),
			"wide cycle lost a built-in wide-gauge bogey");
		require(!wide.contains(AllBogeyStyles.STANDARD)
			&& !wide.contains(CRBogeyStyles.NARROW_DEFAULT),
			"wide cycle contains an incompatible bogey");
		require(narrow.contains(CRBogeyStyles.NARROW_DEFAULT)
			&& narrow.contains(CRBogeyStyles.NARROW_DOUBLE_SCOTCH),
			"narrow cycle lost a built-in narrow-gauge bogey");
		require(!narrow.contains(AllBogeyStyles.STANDARD)
			&& !narrow.contains(CRBogeyStyles.WIDE_DEFAULT),
			"narrow cycle contains an incompatible bogey");

		requireAllFit(standard, CRTrackMaterials.CRTrackType.STANDARD);
		requireAllFit(wide, CRTrackMaterials.CRTrackType.WIDE_GAUGE);
		requireAllFit(narrow, CRTrackMaterials.CRTrackType.NARROW_GAUGE);
	}

	private static void checkContextualStyleCycle(ServerLevel level) {
		BlockPos trackPos = railways$findAirPair(level);
		BlockPos bogeyPos = trackPos.above();
		TrackMaterial[] materials = {
			AllTrackMaterials.ANDESITE,
			CRTrackMaterials.WIDE_GAUGE_ANDESITE,
			CRTrackMaterials.NARROW_GAUGE_ANDESITE
		};
		BogeyStyle[] incompatibleStarts = {
			CRBogeyStyles.WIDE_DEFAULT,
			CRBogeyStyles.NARROW_DEFAULT,
			CRBogeyStyles.WIDE_DEFAULT
		};

		for (int i = 0; i < materials.length; i++) {
			TrackMaterial material = materials[i];
			BogeyStyle start = incompatibleStarts[i];
			AbstractBogeyBlock<?> bogeyBlock = start.getBlockForSize(AllBogeySizes.SMALL);
			try {
				BlockState trackState = material.getBlock().defaultBlockState()
					.setValue(TrackBlock.SHAPE, TrackShape.XO);
				BlockState bogeyState = bogeyBlock.defaultBlockState()
					.setValue(AbstractBogeyBlock.AXIS, Direction.Axis.X);
				require(level.setBlock(trackPos, trackState, 3), "could not place a temporary gauge probe track");
				require(level.setBlock(bogeyPos, bogeyState, 3), "could not place a temporary gauge probe bogey");
				var blockEntity = level.getBlockEntity(bogeyPos);
				require(blockEntity instanceof AbstractBogeyBlockEntity,
					"temporary gauge probe bogey has no block entity");
				AbstractBogeyBlockEntity bogeyBE = (AbstractBogeyBlockEntity) blockEntity;
				bogeyBE.setBogeyStyle(start);

				Identifier trackType = CRTrackMaterials.getType(material);
				BogeyStyle next = bogeyBlock.getNextStyle(level, bogeyPos);
				require(CRBogeyStyles.styleFitsTrack(next, trackType),
					"contextual wrench cycle returned an incompatible style for " + trackType);
			} finally {
				level.setBlock(bogeyPos, Blocks.AIR.defaultBlockState(), 3);
				level.setBlock(trackPos, Blocks.AIR.defaultBlockState(), 3);
			}
		}
	}

	private static BlockPos railways$findAirPair(ServerLevel level) {
		for (int y = level.getMaxY() - 2; y >= Math.max(level.getMinY(), level.getMaxY() - 64); y--) {
			BlockPos trackPos = new BlockPos(0, y, 0);
			if (level.getBlockState(trackPos).isAir() && level.getBlockState(trackPos.above()).isAir())
				return trackPos;
		}
		throw new IllegalStateException("Bogey gauge runtime check failed: no air pair for wrench-cycle probe");
	}

	private static void checkTrackAnchors(MinecraftServer server) {
		TrackMaterial[] materials = {
			AllTrackMaterials.ANDESITE,
			CRTrackMaterials.WIDE_GAUGE_ANDESITE,
			CRTrackMaterials.NARROW_GAUGE_ANDESITE
		};
		BogeyStyle[] selectedStyles = {
			AllBogeyStyles.STANDARD,
			CRBogeyStyles.WIDE_DEFAULT,
			CRBogeyStyles.NARROW_DEFAULT
		};

		try {
			BogeyMenuHandlerServer.setCurrentPlayer(PROBE_PLAYER);
			for (BogeyStyle selected : selectedStyles) {
				BogeyMenuHandlerServer.addStyle(PROBE_PLAYER, Pair.of(selected, AllBogeySizes.SMALL));
				for (TrackMaterial material : materials) {
					TrackBlock track = material.getBlock();
					BlockState trackState = track.defaultBlockState()
						.setValue(TrackBlock.SHAPE, TrackShape.XO);
					BlockState anchor = track.getBogeyAnchor(server.overworld(), BlockPos.ZERO, trackState);
					Identifier trackType = CRTrackMaterials.getType(material);
					BogeyStyle resolved = resolve(selected, trackType);
					AbstractBogeyBlock<?> expectedBlock = resolved.getBlockForSize(AllBogeySizes.SMALL);

					require(anchor.getBlock() == expectedBlock,
						"track anchor returned the wrong physical bogey block for " + trackType);
					require(expectedBlock.getValidPathfindingTypes(resolved).contains(trackType)
						|| expectedBlock.getValidPathfindingTypes(resolved)
							.contains(CRTrackMaterials.CRTrackType.UNIVERSAL),
						"track anchor returned an incompatible physical bogey block for " + trackType);
				}
			}
		} finally {
			BogeyMenuHandlerServer.setCurrentPlayer(null);
			BogeyMenuHandlerServer.removeStyle(PROBE_PLAYER);
		}
	}

	private static BogeyStyle resolve(BogeyStyle selected, Identifier trackType) {
		return CRBogeyStyles.getMapped(selected, trackType, true)
			.filter(style -> CRBogeyStyles.styleFitsTrack(style, trackType))
			.orElseThrow(() -> new IllegalStateException(
				"Bogey gauge runtime check failed: no compatible style for " + trackType
			));
	}

	private static void requireAllFit(List<BogeyStyle> styles, Identifier trackType) {
		require(!styles.isEmpty(), "cycle filter returned no styles for " + trackType);
		for (BogeyStyle style : styles)
			require(CRBogeyStyles.styleFitsTrack(style, trackType),
				"cycle filter retained an incompatible style for " + trackType);
	}

	private static void require(boolean condition, String message) {
		if (!condition)
			throw new IllegalStateException("Bogey gauge runtime check failed: " + message);
	}
}
