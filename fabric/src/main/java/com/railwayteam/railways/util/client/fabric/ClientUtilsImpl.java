package com.railwayteam.railways.util.client.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

@Environment(EnvType.CLIENT)
public class ClientUtilsImpl {
    public static boolean isActiveAndMatches(KeyMapping mapping, InputConstants.Key keyCode) {
        return mapping != null && KeyBindingHelper.getBoundKeyOf(mapping).equals(keyCode);
    }
}
