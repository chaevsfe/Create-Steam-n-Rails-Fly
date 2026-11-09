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

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.MixinVariables;
import com.zurrtum.create.content.trains.entity.Navigation;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Set;

@Mixin(value = Navigation.class, remap = false)
public class MixinNavigation {
	@WrapOperation(
		method = "search(DDZLjava/util/ArrayList;Lcom/zurrtum/create/content/trains/entity/Navigation$StationTest;)V",
		at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z")
	)
	private boolean railways$acceptMappedTrackType(Set<Identifier> validTypes, Object materialId, Operation<Boolean> original) {
		if (original.call(validTypes, materialId))
			return true;
		if (!(materialId instanceof Identifier id))
			return false;

		TrackMaterial material = TrackMaterial.fromId(id);
		if (material == null)
			return false;

		Identifier trackType = CRTrackMaterials.getType(material);
		return validTypes.contains(trackType) || CRTrackMaterials.CRTrackType.UNIVERSAL.equals(trackType);
	}

	@WrapMethod(
		method = "search(DDZLjava/util/ArrayList;Lcom/zurrtum/create/content/trains/entity/Navigation$StationTest;)V"
	)
	private void railways$trackNavigationDepth(
		double maxDistance,
		double maxCost,
		boolean forward,
		ArrayList<GlobalStation> destinations,
		Navigation.StationTest stationTest,
		Operation<Void> original
	) {
		MixinVariables.navigationCallDepth++;
		try {
			original.call(maxDistance, maxCost, forward, destinations, stationTest);
		} finally {
			MixinVariables.navigationCallDepth--;
		}
	}
}
