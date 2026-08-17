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
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.railwayteam.railways.mixin_interfaces.ITrueMaxSpeedTrain;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainRealism implements ITrueMaxSpeedTrain {
    @Shadow
    public double speed;

    @Shadow
    public int fuelTicks;

    @Unique
    private boolean railways$skipRealismSpeedLimit = false;

    @Override
    public void railways$setLimitBypass(boolean shouldBypass) {
        railways$skipRealismSpeedLimit = shouldBypass;
    }

    @Unique
    private boolean railways$realismLimited() {
        return !railways$skipRealismSpeedLimit
            && fuelTicks <= 0
            && !((IHandcarTrain) this).railways$isHandcar()
            && CRConfigs.server().realism.realisticTrains.get();
    }

    @Inject(method = "maxSpeed", at = @At("RETURN"), cancellable = true)
    private void railways$limitMaxSpeed(CallbackInfoReturnable<Float> cir) {
        if (((IHandcarTrain) this).railways$isHandcar())
            cir.setReturnValue(cir.getReturnValue() * 0.5f);
        else if (railways$realismLimited())
            cir.setReturnValue(AllConfigs.server().trains.trainTopSpeed.getF() / (20 * 20));
    }

    @Inject(method = "maxTurnSpeed", at = @At("RETURN"), cancellable = true)
    private void railways$limitMaxTurnSpeed(CallbackInfoReturnable<Float> cir) {
        if (((IHandcarTrain) this).railways$isHandcar())
            cir.setReturnValue(cir.getReturnValue() * 0.75f);
        else if (railways$realismLimited())
            cir.setReturnValue(AllConfigs.server().trains.trainTurningTopSpeed.getF() / (20 * 20));
    }

    @WrapOperation(
        method = "approachTargetSpeed",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/trains/entity/Train;acceleration()F")
    )
    private float railways$limitAcceleration(
        Train instance,
        Operation<Float> original,
        @Local(name = "actualTarget") double actualTarget
    ) {
        if (Math.abs(actualTarget) > speed && railways$realismLimited())
            return original.call(instance) / 20;
        return original.call(instance);
    }
}
