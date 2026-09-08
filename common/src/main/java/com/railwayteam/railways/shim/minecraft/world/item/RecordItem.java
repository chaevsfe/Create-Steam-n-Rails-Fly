package com.railwayteam.railways.shim.minecraft.world.item;

import net.minecraft.world.item.Item;

import net.minecraft.sounds.SoundEvent;

public class RecordItem extends Item {
    public RecordItem(Properties properties) {
        super(properties);
    }

    public int getAnalogOutput() {
        return 0;
    }

    public SoundEvent getSound() {
        return null;
    }
}
