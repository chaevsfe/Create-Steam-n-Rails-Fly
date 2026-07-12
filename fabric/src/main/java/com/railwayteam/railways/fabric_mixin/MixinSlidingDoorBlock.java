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
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SlidingDoorBlock.class, remap = false)
public abstract class MixinSlidingDoorBlock {
    @Inject(
        method = "neighborChanged(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/level/redstone/Orientation;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void railways$preventManualModeRedstoneUpdates(
        BlockState state,
        Level level,
        BlockPos pos,
        Block changedBlock,
        @Nullable Orientation orientation,
        boolean moving,
        CallbackInfo ci
    ) {
        if (!railways$getMode(level, pos, state).canOpenSpecially()) {
            ci.cancel();
        }
    }

    @Inject(
        method = "isDoorPowered(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void railways$invertSpecialModePower(
        Level level,
        BlockPos pos,
        BlockState state,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (railways$getMode(level, pos, state) == SlidingDoorMode.SPECIAL_INVERTED) {
            cir.setReturnValue(!cir.getReturnValueZ());
        }
    }

    @Inject(
        method = "useWithoutItem(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void railways$preventSpecialModeManualOpen(
        BlockState state,
        Level level,
        BlockPos pos,
        Player player,
        BlockHitResult hitResult,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!railways$getMode(level, pos, state).canOpenManually()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @Inject(
        method = "setOpen(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void railways$preventSpecialModeEntityOpen(
        @Nullable Entity entity,
        Level level,
        BlockState state,
        BlockPos pos,
        boolean open,
        CallbackInfo ci
    ) {
        if (entity != null && !railways$getMode(level, pos, state).canOpenManually()) {
            ci.cancel();
        }
    }

    @Unique
    private static SlidingDoorMode railways$getMode(Level level, BlockPos pos, BlockState state) {
        BlockPos blockEntityPos = state.getValue(SlidingDoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (level.getBlockEntity(blockEntityPos) instanceof SlidingDoorMode.IHasDoorMode doorMode) {
            return doorMode.railways$getSlidingDoorMode();
        }
        return SlidingDoorMode.NORMAL;
    }
}
