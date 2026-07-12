/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.conductor_possession;

import com.railwayteam.railways.content.conductor.ClientHandler;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevents the stationary player's hands/items from being rendered through the conductor camera. */
@Mixin(ItemInHandRenderer.class)
public class MixinItemInHandRenderer {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void railways$hidePlayerHandsWhilePossessing(CallbackInfo ci) {
        if (ClientHandler.isPlayerMountedOnCamera())
            ci.cancel();
    }
}
