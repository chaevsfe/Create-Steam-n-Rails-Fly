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

import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(BlockBehaviour.class)
public class MixinBlockBehaviourTrackCasingDrops {
    @Inject(method = "getDrops", at = @At("RETURN"), cancellable = true)
    private void railways$addTrackCasingDrops(BlockState state, LootParams.Builder params,
                                              CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!(params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof IHasTrackCasing casing))
            return;
        Block trackCasing = casing.railways$getTrackCasing();
        if (trackCasing == null)
            return;
        List<ItemStack> drops = new ArrayList<>(cir.getReturnValue());
        drops.add(new ItemStack(trackCasing));
        cir.setReturnValue(drops);
    }
}
