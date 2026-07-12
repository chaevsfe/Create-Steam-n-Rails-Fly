package com.tterrag.registrate.builders;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntityBuilder<T extends Entity, P> extends AbstractBuilder<EntityType<T>, P, EntityBuilder<T, P>> {
    private final EntityType.EntityFactory<T> factory;
    private final MobCategory category;
    private float width = 0.6f;
    private float height = 1.8f;
    private Consumer<Object> properties = ignored -> {};
    private Supplier<AttributeSupplier.Builder> attributes;

    public EntityBuilder(Registrate owner, String name, EntityType.EntityFactory<T> factory, MobCategory category) {
        super(owner, name, null);
        this.factory = factory;
        this.category = category;
    }

    @SuppressWarnings("unchecked")
    public EntityBuilder<T, P> properties(java.util.function.Consumer<?> consumer) {
        this.properties = this.properties.andThen((Consumer<Object>) consumer);
        return this;
    }

    public EntityBuilder<T, P> attributes(Supplier<AttributeSupplier.Builder> attributes) {
        this.attributes = attributes;
        return this;
    }

    public EntityBuilder<T, P> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public EntityEntry<T> register() {
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, owner.id(name));
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, category)
            .sized(width, height);
        properties.accept(builder);
        EntityType<T> type = builder.build(key);
        owner.registerVanilla(BuiltInRegistries.ENTITY_TYPE, name, type);
        if (attributes != null)
            FabricDefaultAttributeRegistry.register((EntityType) type, attributes.get());
        return new EntityEntry<>(owner.id(name), type);
    }
}
