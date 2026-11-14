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

import com.railwayteam.railways.content.custom_bogeys.special.invisible.InvisibleBogeyBlock;
import com.railwayteam.railways.content.custom_bogeys.special.monobogey.InvisibleMonoBogeyBlock;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CarriageBogey.class, remap = false)
public class MixinCarriageBogeyInvisibleCoupler {
    @Shadow
    public AbstractBogeyBlock<?> type;

    @Shadow
    public Couple<Vec3> couplingAnchors;

    @Inject(method = "updateCouplingAnchor(Lnet/minecraft/world/phys/Vec3;FFIFFZ)V", at = @At("HEAD"), cancellable = true)
    private void railways$hideInvisibleCoupler(Vec3 entityPos, float entityXRot, float entityYRot, int bogeySpacing,
                                               float yaw, float pitch, boolean leading, CallbackInfo ci) {
        if (type instanceof InvisibleBogeyBlock || type instanceof InvisibleMonoBogeyBlock) {
            couplingAnchors.set(leading, null);
            ci.cancel();
        }
    }
}
