package com.railwayteam.railways.content.conductor.toolbox.fabric;

import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import net.minecraft.server.level.ServerPlayer;

public class MountedToolboxImpl {
    public static void openMenu(ServerPlayer player, MountedToolbox toolbox) {
        MenuProvider.openHandledScreen(player, toolbox);
    }
}
