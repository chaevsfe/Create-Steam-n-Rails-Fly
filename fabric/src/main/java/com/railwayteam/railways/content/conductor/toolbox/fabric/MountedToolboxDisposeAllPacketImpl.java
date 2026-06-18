package com.railwayteam.railways.content.conductor.toolbox.fabric;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.UUID;

public class MountedToolboxDisposeAllPacketImpl {
    public static boolean doDisposal(MountedToolbox toolbox, ServerPlayer player, ConductorEntity conductor) {
        CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);
        MutableBoolean sendData = new MutableBoolean(false);
        ToolboxInventory inv = toolbox.inventory;
        inv.inLimitedMode(inventory -> {
            for (int i = 0; i < 36; i++) {
                String key = String.valueOf(i);
                compound.getCompound(key).ifPresent(data -> {
                    UUID uuid = readUuid(data);
                    if (conductor.getUUID().equals(uuid)) {
                        ToolboxHandler.unequip(player, Integer.parseInt(key), true);
                        sendData.setTrue();
                    }
                });

                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty())
                    continue;
                ItemStack remaining = stack.copy();
                for (int compartment = 0; compartment < 8 && !remaining.isEmpty(); compartment++) {
                    int inserted = inventory.distributeToCompartment(remaining, compartment, false);
                    if (inserted > 0)
                        remaining.shrink(inserted);
                }
                if (remaining.getCount() != stack.getCount())
                    player.getInventory().setItem(i, remaining);
            }
        });
        return sendData.booleanValue();
    }

    private static UUID readUuid(CompoundTag data) {
        try {
            return data.getString("EntityUUID").map(UUID::fromString).orElse(null);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
