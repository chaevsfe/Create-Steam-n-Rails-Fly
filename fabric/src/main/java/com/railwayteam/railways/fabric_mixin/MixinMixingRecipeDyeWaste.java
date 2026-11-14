/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.railwayteam.railways.content.palettes.painting.DyeingBasinInput;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.basin.BasinInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = MixingRecipe.class, remap = false)
public class MixinMixingRecipeDyeWaste {
    @WrapMethod(method = "matches(Lcom/zurrtum/create/content/processing/basin/BasinInput;Lnet/minecraft/world/level/Level;)Z")
    private boolean railways$skipDyedMatches(BasinInput input, Level level, Operation<Boolean> original) {
        return original.call(DyeingBasinInput.mask((MixingRecipe) (Object) this, input), level);
    }

    @WrapMethod(method = "apply(Lcom/zurrtum/create/content/processing/basin/BasinInput;)Z")
    private boolean railways$skipDyedApply(BasinInput input, Operation<Boolean> original) {
        return original.call(DyeingBasinInput.mask((MixingRecipe) (Object) this, input));
    }
}
