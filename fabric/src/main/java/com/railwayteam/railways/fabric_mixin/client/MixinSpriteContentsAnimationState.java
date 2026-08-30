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

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.mixin_interfaces.IPhantomAnimationState;
import com.railwayteam.railways.mixin_interfaces.IPotentiallyInvisibleSpriteContents;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.AnimationState.class)
public abstract class MixinSpriteContentsAnimationState implements IPhantomAnimationState {

    @Shadow
    private int frame;
    @Shadow
    private boolean isDirty;

    @Unique
    @Nullable
    private SpriteContents railways$phantomOwner;

    @Override
    public void railways$setPhantomOwner(@Nullable SpriteContents owner) {
        this.railways$phantomOwner = owner;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void railways$pinPhantomFrame(CallbackInfo ci) {
        if (railways$phantomOwner == null)
            return;
        IPotentiallyInvisibleSpriteContents contents = (IPotentiallyInvisibleSpriteContents) railways$phantomOwner;
        if (!contents.railways$shouldDoInvisibility())
            return;
        int target = contents.railways$isVisible() ? 0 : 1;
        isDirty = frame != target;
        frame = target;
        ci.cancel();
    }
}
