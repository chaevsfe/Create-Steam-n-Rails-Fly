/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

import com.railwayteam.railways.content.extended_sliding_doors.SlidingDoorMode;
import com.railwayteam.railways.content.extended_sliding_doors.SlidingDoorModeServerBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = SlidingDoorBlockEntity.class, remap = false)
public abstract class MixinSlidingDoorBlockEntity implements SlidingDoorMode.IHasDoorMode {
    @Unique
    private SlidingDoorModeServerBehaviour railways$doorMode;

    @Inject(method = "addBehaviours(Ljava/util/List;)V", at = @At("RETURN"))
    private void railways$addDoorModeBehaviour(List<BlockEntityBehaviour<?>> behaviours, CallbackInfo ci) {
        SlidingDoorBlockEntity blockEntity = (SlidingDoorBlockEntity) (Object) this;
        railways$doorMode = new SlidingDoorModeServerBehaviour(blockEntity);
        behaviours.add(railways$doorMode);
    }

    @Override
    public SlidingDoorMode railways$getSlidingDoorMode() {
        return railways$doorMode == null ? SlidingDoorMode.NORMAL : railways$doorMode.get();
    }
}
