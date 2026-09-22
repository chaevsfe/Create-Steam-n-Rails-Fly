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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlock;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlockEntity;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.TrackShapeLookup;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.TrackShapeLookup.GenericCrossingData;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.registry.CRTrackMaterials.CRTrackType;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackPlacement;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TrackPlacement.class, remap = false)
public class MixinTrackPlacement {
    @ModifyExpressionValue(method = "tryConnect", at = {
        @At(value = "CONSTANT", args = "doubleValue=7.0"),
        @At(value = "CONSTANT", args = "doubleValue=3.25")
    })
    private static double railways$modifiedCurvesForGauges(double value, @Local(argsOnly = true) ItemStack stack) {
        Identifier trackType = CRTrackMaterials.getType(TrackMaterial.fromItem(stack.getItem()));
        if (CRTrackType.WIDE_GAUGE.equals(trackType))
            return value * 2;
        if (CRTrackType.NARROW_GAUGE.equals(trackType) || CRTrackType.UNIVERSAL.equals(trackType))
            return value * 0.5;
        return value;
    }

    @WrapOperation(
        method = "placeTracks",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/trains/track/ITrackBlock;overlay(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/block/state/BlockState;"
        )
    )
    private static BlockState railways$placeGenericCrossing(ITrackBlock track, BlockGetter world, BlockPos pos,
                                                            BlockState existing, BlockState placed,
                                                            Operation<BlockState> original,
                                                            @Share("crossingData") LocalRef<GenericCrossingData> crossingData) {
        BlockState result = original.call(track, world, pos, existing, placed);
        crossingData.set(null);
        if (result != existing || !existing.hasProperty(TrackBlock.SHAPE) || !placed.hasProperty(TrackBlock.SHAPE))
            return result;
        if (!(existing.getBlock() instanceof ITrackBlock existingTrack) || !(placed.getBlock() instanceof ITrackBlock overlayTrack))
            return result;

        Pair<TrackShape, Boolean> merged = TrackShapeLookup.getMerged(existing.getValue(TrackBlock.SHAPE), placed.getValue(TrackBlock.SHAPE));
        if (merged == null)
            return result;

        crossingData.set(new GenericCrossingData(merged, existingTrack.getMaterial(), overlayTrack.getMaterial()));
        return CRBlocks.GENERIC_CROSSING.getDefaultState().setValue(GenericCrossingBlock.SHAPE, merged.getFirst());
    }

    @WrapOperation(
        method = "placeTracks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            ordinal = 0
        )
    )
    private static boolean railways$initGenericCrossing(Level level, BlockPos pos, BlockState state, int flags,
                                                        Operation<Boolean> original,
                                                        @Share("crossingData") LocalRef<GenericCrossingData> crossingData) {
        boolean placed = original.call(level, pos, state, flags);
        GenericCrossingData data = crossingData.get();
        crossingData.set(null);
        if (data != null && level.getBlockEntity(pos) instanceof GenericCrossingBlockEntity crossing)
            crossing.initFrom(data);
        return placed;
    }

    @Inject(method = "placeTracks", at = @At("HEAD"), cancellable = true)
    private static void railways$preventCurveIntoJunction(Level level, TrackPlacement.PlacementInfo info,
                                                          BlockState state1, BlockState state2,
                                                          BlockPos targetPos1, BlockPos targetPos2, boolean simulate,
                                                          CallbackInfoReturnable<TrackPlacement.PlacementInfo> cir) {
        if (info.curve == null)
            return;
        if (level.getBlockState(targetPos1).getBlock() instanceof GenericCrossingBlock
            || level.getBlockState(targetPos2).getBlock() instanceof GenericCrossingBlock) {
            info.withMessage("junction_start");
            info.valid = false;
            cir.setReturnValue(info);
        }
    }
}
