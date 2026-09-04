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

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import com.railwayteam.railways.mixin_interfaces.IPhantomAnimationState;
import com.railwayteam.railways.mixin_interfaces.IPotentiallyInvisibleSpriteContents;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpriteContents.class)
public abstract class MixinSpriteContents implements IPotentiallyInvisibleSpriteContents {

    @Unique
    private boolean railways$visible = true;
    @Unique
    private boolean railways$shouldDoInvisibility = false;

    @Inject(method = "<init>(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/metadata/animation/FrameSize;Lcom/mojang/blaze3d/platform/NativeImage;Ljava/util/Optional;Ljava/util/List;Ljava/util/Optional;)V", at = @At("RETURN"))
    private void railways$onInit(CallbackInfo ci) {
        if (PhantomSpriteManager.register((SpriteContents) (Object) this))
            railways$shouldDoInvisibility = true;
    }

    @Inject(method = "createAnimationState", at = @At("RETURN"))
    private void railways$tagAnimationState(GpuBufferSlice ubo, int mipLevels, CallbackInfoReturnable<SpriteContents.AnimationState> cir) {
        if (!railways$shouldDoInvisibility)
            return;
        SpriteContents.AnimationState state = cir.getReturnValue();
        if (state != null) {
            ((IPhantomAnimationState) state).railways$setPhantomOwner((SpriteContents) (Object) this);
            PhantomSpriteManager.countTaggedState();
        }
    }

    @Override
    public void railways$uploadFrame(boolean visible) {
        this.railways$visible = visible;
    }

    @Override
    public boolean railways$shouldDoInvisibility() {
        return railways$shouldDoInvisibility;
    }

    @Override
    public boolean railways$isVisible() {
        return railways$visible || !railways$shouldDoInvisibility;
    }
}
