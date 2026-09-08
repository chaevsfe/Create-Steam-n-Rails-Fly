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

package com.railwayteam.railways.content.palettes.ct;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.registry.CRPalettes.Styles;
import com.railwayteam.railways.registry.CRPalettes.WindowType;
import com.railwayteam.railways.registry.CRPalettes.Wrapping;
import com.railwayteam.railways.registry.CRSpriteShifts;
import com.railwayteam.railways.shim.registrate.util.entry.BlockEntry;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.client.foundation.block.connected.SimpleCTBehaviour;
import com.zurrtum.create.client.infrastructure.model.CTModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.EnumMap;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class PaletteConnectedTextures {

    private static final int STYLES_PER_COLOR = 19;

    private static int bound;
    private static int skipped;

    public static void register() {
        bound = 0;
        skipped = 0;
        for (PalettesColor color : PalettesColor.values()) {
            simple(Styles.SLASHED, color, CRSpriteShifts.SLASHED_LOCOMETAL);
            simple(Styles.RIVETED, color, CRSpriteShifts.RIVETED_LOCOMETAL);
            simple(Styles.VENT, color, CRSpriteShifts.LOCOMETAL_VENT);
            simple(Styles.BRASS_WRAPPED_SLASHED, color, CRSpriteShifts.BRASS_WRAPPED_LOCOMETAL);
            simple(Styles.COPPER_WRAPPED_SLASHED, color, CRSpriteShifts.COPPER_WRAPPED_LOCOMETAL);
            simple(Styles.IRON_WRAPPED_SLASHED, color, CRSpriteShifts.IRON_WRAPPED_LOCOMETAL);

            pillar(Styles.PILLAR, color, CRSpriteShifts.RIVETED_LOCOMETAL_PILLAR);
            pillar(Styles.SMOKEBOX, color, CRSpriteShifts.getSmokebox(null));
            pillar(Styles.BRASS_WRAPPED_SMOKEBOX, color, CRSpriteShifts.getSmokebox(Wrapping.BRASS));
            pillar(Styles.COPPER_WRAPPED_SMOKEBOX, color, CRSpriteShifts.getSmokebox(Wrapping.COPPER));
            pillar(Styles.IRON_WRAPPED_SMOKEBOX, color, CRSpriteShifts.getSmokebox(Wrapping.IRON));

            window(Styles.ROUND_PANE_WINDOW, color, WindowType.ROUND_PANE);
            window(Styles.SINGLE_PANE_WINDOW, color, WindowType.SINGLE_PANE);
            window(Styles.TWO_PANE_WINDOW, color, WindowType.TWO_PANE);
            window(Styles.FOUR_PANE_WINDOW, color, WindowType.FOUR_PANE);

            boiler(Styles.BOILER, color, CRSpriteShifts.BOILER_SIDE);
            boiler(Styles.BRASS_WRAPPED_BOILER, color, CRSpriteShifts.BRASS_WRAPPED_BOILER_SIDE);
            boiler(Styles.COPPER_WRAPPED_BOILER, color, CRSpriteShifts.COPPER_WRAPPED_BOILER_SIDE);
            boiler(Styles.IRON_WRAPPED_BOILER, color, CRSpriteShifts.IRON_WRAPPED_BOILER_SIDE);
        }

        int expected = STYLES_PER_COLOR * PalettesColor.values().length;
        if (bound == 0)
            throw new IllegalStateException(
                "Palette connected textures bound to 0 of " + expected + " blocks"
            );
        Railways.LOGGER.info("Palette connected textures registered for {} of {} blocks{}", bound, expected,
            skipped == 0 ? "" : " (" + skipped + " skipped, missing block or sprite shift)");
    }

    private static void simple(Styles style, PalettesColor color, EnumMap<PalettesColor, CTSpriteShiftEntry> shifts) {
        bind(style, color, shifts.get(color), SimpleCTBehaviour::new);
    }

    private static void pillar(Styles style, PalettesColor color, EnumMap<PalettesColor, CTSpriteShiftEntry> shifts) {
        bind(style, color, shifts.get(color), PalettesPillarCTBehaviour::new);
    }

    private static void window(Styles style, PalettesColor color, WindowType type) {
        bind(style, color, CRSpriteShifts.WINDOWS.get(type).get(color), PalettesPillarCTBehaviour::new);
    }

    private static void boiler(Styles style, PalettesColor color, EnumMap<PalettesColor, CTSpriteShiftEntry> shifts) {
        bind(style, color, shifts.get(color), BoilerCTBehaviour::new);
    }

    private static void bind(Styles style, PalettesColor color, CTSpriteShiftEntry shift,
                             Function<CTSpriteShiftEntry, ConnectedTextureBehaviour> factory) {
        BlockEntry<?> entry = style.get(color);
        if (entry == null || shift == null) {
            skipped++;
            return;
        }
        AllModels.register(entry.get(), CTModel.of(factory.apply(shift)));
        bound++;
    }
}
