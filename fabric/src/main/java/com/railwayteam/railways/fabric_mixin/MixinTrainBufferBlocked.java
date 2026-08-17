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

import com.railwayteam.railways.mixin_interfaces.IBufferBlockedTrain;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainBufferBlocked implements IBufferBlockedTrain {
    @Shadow
    public double speed;

    @Unique
    private int railways$controlBlockedTicks = -1;

    @Unique
    private int railways$controlBlockedSign = 0;

    @Override
    public boolean railways$isControlBlocked() {
        return railways$controlBlockedTicks > 0;
    }

    @Override
    public void railways$setControlBlocked(boolean controlBlocked, boolean forceBackwards) {
        railways$controlBlockedTicks = controlBlocked ? 3 : -1;
        railways$controlBlockedSign = forceBackwards ? -1 : Mth.sign(speed);
    }

    @Override
    public int railways$getBlockedSign() {
        return railways$controlBlockedSign;
    }

    @Inject(method = "earlyTick", at = @At("HEAD"))
    private void railways$tickControlBlock(Level level, CallbackInfo ci) {
        if (railways$controlBlockedTicks > 0)
            railways$controlBlockedTicks--;
    }
}
