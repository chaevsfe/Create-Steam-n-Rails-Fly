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

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.coupling.TrainUtils;
import com.railwayteam.railways.mixin_interfaces.ICrashAdvancement;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.railwayteam.railways.registry.CRBlocks;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainHandcarCollision implements ICrashAdvancement {
    @Shadow
    public List<Carriage> carriages;

    @Shadow
    public Player backwardsDriver;

    @Inject(
        method = "collideWithOtherTrains",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/trains/entity/Train;crash()V",
            ordinal = 0
        ),
        cancellable = true
    )
    private void railways$handcarCollision(Level level, Carriage carriage, CallbackInfo ci,
                                           @Local Pair<Train, Vec3> collision) {
        Vec3 v = collision.getSecond();

        if (railways$popHandcar((Train) (Object) this, level, v))
            ci.cancel();

        if (railways$popHandcar(collision.getFirst(), level, v))
            ci.cancel();
    }

    @Unique
    private static boolean railways$popHandcar(Train train, Level level, Vec3 v) {
        if (!((IHandcarTrain) train).railways$isHandcar())
            return false;

        if (!train.invalid) {
            TrainUtils.discardTrain(train);
            Containers.dropItemStack(level, v.x, v.y, v.z, CRBlocks.HANDCAR.asStack());
        }

        ((ICrashAdvancement) train).railways$awardCrashAdvancements();
        return true;
    }

    @Override
    public void railways$awardCrashAdvancements() {
        for (Carriage carriage : carriages)
            carriage.forEachPresentEntity(e -> e.getIndirectPassengers()
                .forEach(entity -> {
                    if (!(entity instanceof ServerPlayer p))
                        return;
                    Optional<UUID> controllingPlayer = e.getControllingPlayer();
                    if (controllingPlayer.isPresent() && controllingPlayer.get()
                        .equals(p.getUUID()))
                        return;
                    AllAdvancements.TRAIN_CRASH.trigger(p);
                }));

        if (backwardsDriver instanceof ServerPlayer p)
            AllAdvancements.TRAIN_CRASH_BACKWARDS.trigger(p);
    }
}
