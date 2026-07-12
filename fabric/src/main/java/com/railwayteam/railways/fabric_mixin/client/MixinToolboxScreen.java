/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolboxDisposeAllPacket;
import com.railwayteam.railways.registry.CRPackets;
import com.zurrtum.create.client.content.equipment.toolbox.ToolboxScreen;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.content.equipment.toolbox.ToolboxMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ToolboxScreen.class, remap = false)
public abstract class MixinToolboxScreen extends AbstractSimiContainerScreen<ToolboxMenu> {
    public MixinToolboxScreen(ToolboxMenu container, Inventory inv, Component title) {
        super(
            container,
            inv,
            title,
            30 + AllGuiTextures.TOOLBOX.getWidth(),
            AllGuiTextures.TOOLBOX.getHeight() + AllGuiTextures.PLAYER_INVENTORY.getHeight() - 24
        );
    }

    @Inject(method = "lambda$init$1", at = @At("HEAD"), cancellable = true)
    private void railways$disposeMountedToolbox(CallbackInfo ci) {
        if (menu.contentHolder instanceof MountedToolbox mounted) {
            CRPackets.PACKETS.send(new MountedToolboxDisposeAllPacket(mounted.getParent()));
            ci.cancel();
        }
    }
}
