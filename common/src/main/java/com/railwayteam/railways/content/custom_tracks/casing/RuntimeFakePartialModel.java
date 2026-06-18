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

package com.railwayteam.railways.content.custom_tracks.casing;

import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.railwayteam.railways.mixin.client.AccessorPartialModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.Identifier;

import java.time.Clock;

public class RuntimeFakePartialModel {
  private static Identifier runtime_ify(Identifier loc, BakedModel model) {
    return new Identifier(loc.getNamespace(), "runtime/" + Clock.systemUTC().millis() + "/" + model.hashCode() + "/" + loc.getPath());
  }

    public static PartialModel make(Identifier loc, BakedModel bakedModel) {
        boolean tooLate = AccessorPartialModel.railways$getPopulateOnInit();
        AccessorPartialModel.railways$setPopulateOnInit(false);

        Identifier id = runtime_ify(loc, bakedModel);
        PartialModel partialModel = PartialModel.of(id);
        ((AccessorPartialModel) (Object) partialModel).railways$setBakedModel(bakedModel);

        AccessorPartialModel.railways$getALL().remove(id);
        AccessorPartialModel.railways$setPopulateOnInit(tooLate);

        return partialModel;
    }
}
