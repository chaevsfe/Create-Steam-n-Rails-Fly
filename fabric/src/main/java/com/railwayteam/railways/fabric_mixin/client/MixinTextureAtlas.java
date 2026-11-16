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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomSpriteManager;
import com.railwayteam.railways.mixin_interfaces.IPhantomAnimationState;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas {
    @Shadow
    @Final
    private Identifier location;
    @Shadow
    private List<SpriteContents.AnimationState> animatedTexturesStates;

    @ModifyExpressionValue(method = "uploadAnimationFrames", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"))
    private boolean railways$drawPendingPhantomFrames(boolean original) {
        if (original)
            return true;
        for (SpriteContents.AnimationState state : animatedTexturesStates) {
            if (((IPhantomAnimationState) state).railways$hasPendingPhantomFrame())
                return true;
        }
        return false;
    }

    @WrapOperation(method = "uploadAnimationFrames", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/SpriteContents$AnimationState;needsToDraw()Z"))
    private boolean railways$drawPendingPhantomFrame(SpriteContents.AnimationState state, Operation<Boolean> original) {
        return original.call(state) || ((IPhantomAnimationState) state).railways$hasPendingPhantomFrame();
    }

    @Inject(method = "cycleAnimationFrames", at = @At("HEAD"))
    private void railways$cycleAnimationFrames(CallbackInfo ci) {
        if (TextureAtlas.LOCATION_BLOCKS.equals(this.location))
            PhantomSpriteManager.renderTick();
    }

    @Inject(method = "upload", at = @At("RETURN"))
    private void railways$onAtlasUploaded(SpriteLoader.Preparations preparations, CallbackInfo ci) {
        if (TextureAtlas.LOCATION_BLOCKS.equals(this.location))
            PhantomSpriteManager.report();
    }
}
