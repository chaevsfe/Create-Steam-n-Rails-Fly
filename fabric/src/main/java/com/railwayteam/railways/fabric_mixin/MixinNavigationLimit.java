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

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.mixin_interfaces.ILimitedGlobalStation;
import com.zurrtum.create.content.trains.entity.Navigation;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.station.GlobalStation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Navigation.class, remap = false)
public abstract class MixinNavigationLimit {
    @Shadow
    public Train train;

    @WrapOperation(
        method = "search(DDZLjava/util/ArrayList;Lcom/zurrtum/create/content/trains/entity/Navigation$StationTest;)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/trains/station/GlobalStation;getPresentTrain()Lcom/zurrtum/create/content/trains/entity/Train;"
        )
    )
    private Train railways$replacePresentTrain(GlobalStation instance, Operation<Train> original) {
        return ((ILimitedGlobalStation) instance).orDisablingTrain(original.call(instance), train);
    }
}
