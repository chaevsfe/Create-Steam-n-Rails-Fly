package com.railwayteam.railways.util.client.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

@Environment(EnvType.CLIENT)
public class ClientUtilsImpl {
    public static boolean isActiveAndMatches(KeyMapping mapping, InputConstants.Key keyCode) {
        return mapping != null && KeyMappingHelper.getBoundKeyOf(mapping).equals(keyCode);
    }
}
