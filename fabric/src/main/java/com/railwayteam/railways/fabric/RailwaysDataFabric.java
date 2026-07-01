/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.compat.Mods;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;

public class RailwaysDataFabric implements DataGeneratorEntrypoint {
	public void onInitializeDataGenerator(FabricDataGenerator gen) {
		// Ensure that all mods are present if they are needed for data gen
		for (Mods mod : Mods.values())
			mod.assertForDataGen();

		FabricDataGenerator.Pack pack = gen.createPack();
		Railways.gatherData(pack);
	}
	public void buildRegistry(RegistrySetBuilder builder) {
	}
}
