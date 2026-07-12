/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

import com.railwayteam.railways.base.data.CRTagGen;
import com.railwayteam.railways.base.data.compat.emi.EmiExcludedTagGen;
import com.railwayteam.railways.base.data.compat.emi.EmiRecipeDefaultsGen;
import com.railwayteam.railways.base.registration.MultiRegistryCallback;
import com.railwayteam.railways.compat.Mods;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.multiloader.CommandRegistrar;
import com.railwayteam.railways.multiloader.Env;
import com.railwayteam.railways.multiloader.Loader;
import com.railwayteam.railways.registry.CRAdvancements;
import com.railwayteam.railways.registry.CRCommands;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.util.Utils;
import com.railwayteam.railways.content.item.RailwaysTooltipModifiers;
import com.zurrtum.create.CreateBuildInfo;
import com.zurrtum.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public class Railways {
  public static final String MOD_ID = "railways";
  public static final String ID_NAME = "Railways";
  public static final String NAME = "Steam 'n' Rails";
  public static final Logger LOGGER = LoggerFactory.getLogger(ID_NAME);
  /*
   Only used for datafixers, bump whenever a block changes id etc.
   Should be bumped up to the next multiple of 10 the first time it is bumped after a release, then by 1 for each subsequent change.
   Versions:
   10: 1.7.0-rc.1
   11: 1.7.0-rc.2
  */
  public static final int DATA_FIXER_VERSION = 11;
  private static final boolean FORCE_MIXIN_AUDIT = Boolean.getBoolean("railways.force_mixin_audit");

  private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

  static {
    Env.CLIENT.runIfCurrent(() -> RailwaysTooltipModifiers::register);
  }

  private static void migrateConfig(Path path, Function<String, String> converter) {
    Convert: try {

      String str = new String(Files.readAllBytes(path));
      if (str.contains("#General settings") || str.contains("[general]")) { // we found a legacy config
        String migrated;
        try {
          migrated = converter.apply(new String(Files.readAllBytes(path)));
        } catch (IOException e) {
          break Convert;
        }
        try (FileWriter writer = new FileWriter(path.toFile())) {
          writer.write(migrated);
        }
      }
    } catch (IOException ignored) {}
  }

  public static void init() {
    LOGGER.info("{} v{} initializing! Commit hash: {} on Create version: {} on platform: {}", NAME, RailwaysBuildInfo.VERSION, RailwaysBuildInfo.GIT_COMMIT, CreateBuildInfo.VERSION, Loader.getFormatted());
    Path configDir = Utils.configDir();
    Path clientConfigDir = configDir.resolve(MOD_ID + "-client.toml");
    migrateConfig(clientConfigDir, CRConfigs::migrateClient);

    Path commonConfigDir = configDir.resolve(MOD_ID + "-common.toml");
    migrateConfig(commonConfigDir, CRConfigs::migrateCommon);

    ModSetup.register();
    finalizeRegistrate();

    registerCommands(CRCommands::register);
    CRPackets.PACKETS.registerC2SListener();

    // everything should be registered (or at least loaded) by now.
    MultiRegistryCallback.enableFinalizers();

    // TODO - Forge entirely breaks with mixin audit, truly incredible
    if (FORCE_MIXIN_AUDIT || Utils.isDevEnv() && !Loader.FORGE.isCurrent() && !Mods.BYG.isLoaded && !Mods.SODIUM.isLoaded && !Utils.isEnvVarTrue("DATAGEN")) // force all mixins to load in dev
      MixinEnvironment.getCurrentEnvironment().audit();
  }

  public static void postRegistrationInit() {
    ModSetupLate.registerPostRegistration();
  }

  public static Identifier asResource(String name) {
    return Identifier.fromNamespaceAndPath(MOD_ID, name);
  }

  public static void gatherData(DataGenerator.PackGenerator gen) {
    REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, CRTagGen::generateBlockTags);
    REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, CRTagGen::generateItemTags);
    // TODO 1.21: recipe/lang/hat datagen depends on APIs removed by Create 6 / Minecraft 1.21.
    // Keep runtime compilation moving; restore these providers after the client launches.

    gen.addProvider(CRAdvancements::new);
    gen.addProvider(EmiExcludedTagGen::new);
    gen.addProvider(EmiRecipeDefaultsGen::new);
  }

  public static CreateRegistrate registrate() {
    return REGISTRATE;
  }

  public static void finalizeRegistrate() {
    com.railwayteam.railways.fabric.RailwaysImpl.finalizeRegistrate();
  }

  public static void registerCommands(CommandRegistrar registrar) {
    com.railwayteam.railways.fabric.RailwaysImpl.registerCommands(registrar);
  }

  public static void platformBasedRegistration() {
    com.railwayteam.railways.fabric.RailwaysImpl.platformBasedRegistration();
  }
}
