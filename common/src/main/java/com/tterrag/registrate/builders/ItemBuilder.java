package com.tterrag.registrate.builders;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public class ItemBuilder<T extends Item, P> extends AbstractBuilder<T, P, ItemBuilder<T, P>> {
    private final Function<Item.Properties, T> factory;
    private Function<Item.Properties, Item.Properties> properties = Function.identity();
    private boolean tabOverridden = false;
    private ResourceKey<CreativeModeTab> tabOverride = null;
    private boolean callbacksRun = false;
    private boolean itemCallbacksRun = false;

    public ItemBuilder(Registrate owner, String name, Function<Item.Properties, T> factory, P parent) {
        super(owner, name, parent);
        this.factory = factory;
    }

    public ItemBuilder<T, P> properties(Function<Item.Properties, Item.Properties> transformer) {
        this.properties = this.properties.andThen(transformer);
        return this;
    }

    public ItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab) {
        this.tabOverridden = true;
        this.tabOverride = tab;
        return this;
    }

    public ItemBuilder<T, P> removeTab(ResourceKey<CreativeModeTab> tab) {
        return this;
    }

    @Override
    public P build() {
        register();
        return parent;
    }

    @SuppressWarnings("unchecked")
    public ItemEntry<T> register() {
        T existing = BuiltInRegistries.ITEM.getOptional(owner.id(name)).map(i -> (T) i).orElse(null);
        ItemEntry<T> entry;
        if (existing != null) {
            entry = new ItemEntry<>(owner.id(name), existing);
        } else {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, owner.id(name));
            T item = factory.apply(properties.apply(new Item.Properties()).setId(key));
            owner.registerVanilla(BuiltInRegistries.ITEM, name, item);
            entry = new ItemEntry<>(owner.id(name), item);
        }
        ResourceKey<CreativeModeTab> tab = tabOverridden ? tabOverride : owner.getCurrentCreativeTab();
        owner.assignCreativeTab(entry.getId(), tab);
        owner.registerTooltipModifier(entry.get());
        if (!callbacksRun) {
            runRegisterCallbacks(entry.get());
            callbacksRun = true;
        }
        if (!itemCallbacksRun) {
            runAfterRegisterCallbacks(Registries.ITEM, entry.get());
            itemCallbacksRun = true;
        }
        return entry;
    }
}
