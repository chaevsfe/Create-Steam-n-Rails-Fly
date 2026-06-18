package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.registry.CRContainerTypes;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxMenu;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.world.entity.player.Inventory;

public class MountedToolboxContainer extends ToolboxMenu {
    public MountedToolboxContainer(int id, Inventory inv, ToolboxBlockEntity toolbox) {
        super(id, inv, toolbox);
    }

    @Override
    public MenuType<ToolboxBlockEntity> getMenuType() {
        return CRContainerTypes.MOUNTED_TOOLBOX;
    }
}
