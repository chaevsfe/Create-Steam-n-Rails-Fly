package com.tterrag.registrate.util.entry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityEntry<T extends Entity> extends RegistryEntry<EntityType<T>> {
    public EntityEntry(Identifier id, EntityType<T> value) {
        super(id, value);
    }
}
