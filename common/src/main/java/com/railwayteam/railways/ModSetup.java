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

package com.railwayteam.railways;

import com.railwayteam.railways.compat.tracks.mods.*;
import com.railwayteam.railways.content.custom_tracks.casing.CasingCollisionUtils;
import com.railwayteam.railways.multiloader.Env;
import com.railwayteam.railways.registry.*;
import com.railwayteam.railways.registry.fabric.CRBlocksImpl;

public class ModSetup {

  public static void useBaseTab() {
    com.railwayteam.railways.fabric.ModSetupImpl.useBaseTab();
  }

  public static void useTracksTab() {
    com.railwayteam.railways.fabric.ModSetupImpl.useTracksTab();
  }

  public static void usePalettesTab() {
    com.railwayteam.railways.fabric.ModSetupImpl.usePalettesTab();
  }

  /**
   * Registers every Railways block while vanilla is still building its block-state id map.
   *
   * <p>Create Fly invokes this through its {@code create_plugin} entrypoint from the tail of
   * {@code Blocks.<clinit>}, immediately before vanilla iterates the block registry. Registering
   * these blocks from the regular Fabric initializer is too late on 26.2.</p>
   */
  public static void registerBlocksEarly() {
    useBaseTab();
    CRBlockSetTypes.register();
    CRTrackMaterials.register();
    CRCreativeModeTabs.register();
    CRBlocks.register();
    CRPalettes.register();
    CRBlocksImpl.registerEarly();

    // Compat track blocks need to be present for the same vanilla block-state pass.
    useTracksTab();
    HexCastingTrackCompat.register();
    BygTrackCompat.register();
    BlueSkiesTrackCompat.register();
    TwilightForestTrackCompat.register();
    BiomesOPlentyTrackCompat.register();
    NaturesSpiritTrackCompat.register();
    DreamsAndDesiresTrackCompat.register();
    QuarkTrackCompat.register();
    TFCTrackCompat.register();
    useBaseTab();
  }

  /** Register fluids before vanilla builds its fluid-state id map. */
  public static void registerFluidsEarly() {
    CRFluids.register();
  }

  /** Registers entries which do not participate in vanilla's early block/fluid state maps. */
  public static void register() {
    useBaseTab();
    CRBogeyStyles.register();
    CRItems.register();
    Env.CLIENT.runIfCurrent(() -> CRSpriteShifts::register);
    CRDisplaySources.register();
    CRDisplayTargets.register();
    CRBlockEntities.register();
    Railways.platformBasedRegistration();
    CRContainerTypes.register();
    CREntities.register();
    CRSounds.register();
    CRTags.register();
    CREdgePointTypes.register();
    CRSchedule.register();
    CRDataFixers.register();
    CRExtraRegistration.register();
    CRAnimatedFlywheels.register();
    CasingCollisionUtils.register();
    CRInteractionBehaviours.register();
    CRPortalTracks.register();
  }
}
