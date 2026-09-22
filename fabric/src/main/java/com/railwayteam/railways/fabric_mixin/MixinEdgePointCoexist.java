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

import com.railwayteam.railways.registry.CREdgePointTypes;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.signal.SignalBoundary;
import com.zurrtum.create.content.trains.signal.TrackEdgePoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {TrackEdgePoint.class, SignalBoundary.class}, remap = false)
public class MixinEdgePointCoexist {
    @Inject(method = "canCoexistWith", at = @At("RETURN"), cancellable = true)
    private void railways$couplersAndSwitchesCoexist(EdgePointType<?> otherType, boolean front,
                                                     CallbackInfoReturnable<Boolean> cir) {
        EdgePointType<?> type = ((TrackEdgePoint) (Object) this).getType();
        if (type == CREdgePointTypes.COUPLER || otherType == CREdgePointTypes.COUPLER
            || type == CREdgePointTypes.SWITCH || otherType == CREdgePointTypes.SWITCH)
            cir.setReturnValue(true);
    }
}
