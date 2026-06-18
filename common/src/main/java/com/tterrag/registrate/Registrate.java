package com.tterrag.registrate;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.MenuBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class Registrate extends AbstractRegistrate<Registrate> {
    private final Map<String, RegistryEntry<?>> entries = new LinkedHashMap<>();
    private final List<RegistryEntry<?>> orderedEntries = new ArrayList<>();
    private ResourceKey<CreativeModeTab> currentCreativeTab = null;
    // Keyed by ResourceLocation string form: the shim's RegistryEntry builds ResourceLocations via the
    // deprecated constructor, whose instances do not hash/equal reliably across builders.
    private final Map<String, ResourceKey<CreativeModeTab>> creativeTabAssignments = new HashMap<>();

    protected Registrate(String modid) {
        super(modid);
    }

    public void setCurrentCreativeTab(ResourceKey<CreativeModeTab> tab) {
        this.currentCreativeTab = tab;
    }

    public ResourceKey<CreativeModeTab> getCurrentCreativeTab() {
        return currentCreativeTab;
    }

    /** Records which creative tab an entry belongs to. A null tab marks the entry as hidden from all Railways tabs. */
    public void assignCreativeTab(ResourceLocation id, ResourceKey<CreativeModeTab> tab) {
        creativeTabAssignments.put(id.toString(), tab);
    }

    public void registerTooltipModifier(Item item) {
    }

    public boolean isInCreativeTab(RegistryEntry<?> entry, ResourceKey<CreativeModeTab> tab) {
        ResourceKey<CreativeModeTab> assigned = creativeTabAssignments.get(entry.getId().toString());
        return assigned != null && assigned.equals(tab);
    }

    public static Registrate create(String modid) {
        return new Registrate(modid);
    }

    public <T extends Block> BlockBuilder<T, Registrate> block(String name, Function<BlockBehaviour.Properties, T> factory) {
        return new BlockBuilder<>(this, name, factory);
    }

    public <T extends Item> ItemBuilder<T, Registrate> item(String name, Function<Item.Properties, T> factory) {
        return new ItemBuilder<>(this, name, factory, null);
    }

    public <T extends Entity> EntityBuilder<T, Registrate> entity(String name, EntityType.EntityFactory<T> factory, MobCategory category) {
        return new EntityBuilder<>(this, name, factory, category);
    }

    public <T extends BlockEntity> BlockEntityBuilder<T, Registrate> blockEntity(String name, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return new BlockEntityBuilder<>(this, name, factory);
    }

    public <T extends AbstractContainerMenu> MenuBuilder<T, Registrate> menu(String name, NonNullBiFunction<MenuType<T>, Integer, T> factory) {
        return new MenuBuilder<>(this, name, factory);
    }

    public <T extends AbstractContainerMenu, S> MenuBuilder<T, Registrate> menu(String name, MenuBuilder.ForgeMenuFactory<T> factory, java.util.function.Supplier<MenuBuilder.ScreenFactory<T, S>> screenFactory) {
        return new MenuBuilder<T, Registrate>(this, name, factory).screen(screenFactory);
    }

    public Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(modid, name);
    }

    public <V, T extends V> T registerVanilla(Registry<V> registry, String name, T value) {
        T registered = Registry.register(registry, id(name), value);
        RegistryEntry<T> entry = new RegistryEntry<>(id(name), registered);
        entries.put(name, entry);
        orderedEntries.add(entry);
        return registered;
    }

    public <T> RegistryEntry<T> entry(String name, T value) {
        RegistryEntry<T> entry = new RegistryEntry<>(id(name), value);
        entries.put(name, entry);
        orderedEntries.add(entry);
        return entry;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name, Registry<T> registry) {
        Identifier id = id(name);
        if (registry == BuiltInRegistries.BLOCK || registry == BuiltInRegistries.ITEM || registry == BuiltInRegistries.ENTITY_TYPE || registry == BuiltInRegistries.SOUND_EVENT)
            return registry.getValue(id);
        RegistryEntry<?> entry = entries.get(name);
        return entry == null ? null : (T) entry.get();
    }

    public <T> RegistryEntry<T> get(String name, ResourceKey<? extends Registry<T>> registryKey) {
        Registry<T> registry = vanillaRegistry(registryKey);
        T value = registry == null ? null : get(name, registry);
        return value == null ? null : new RegistryEntry<>(id(name), value);
    }

    @SuppressWarnings("unchecked")
    public <T> List<RegistryEntry<T>> getAll(ResourceKey<? extends Registry<T>> registryKey) {
        Registry<T> registry = vanillaRegistry(registryKey);
        List<RegistryEntry<T>> result = new ArrayList<>();
        if (registry == null)
            return result;
        for (RegistryEntry<?> entry : orderedEntries) {
            T value = registry.getValue(entry.getIdentifier());
            if (value == entry.get())
                result.add((RegistryEntry<T>) entry);
        }
        return result;
    }

    public <T> RegistryEntry<T> simple(String name, ResourceKey<? extends Registry<T>> registryKey, java.util.function.Supplier<T> supplier) {
        Registry<T> registry = vanillaRegistry(registryKey);
        if (registry == null)
            return entry(name, supplier.get());
        return entry(name, registerVanilla(registry, name, supplier.get()));
    }

    public <T> void addDataGenerator(ProviderType<T> type, Consumer<T> consumer) {
    }

    public void register() {
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> vanillaRegistry(ResourceKey<? extends Registry<T>> key) {
        Object rawKey = key;
        if (rawKey.equals(Registries.BLOCK))
            return (Registry<T>) BuiltInRegistries.BLOCK;
        if (rawKey.equals(Registries.ITEM))
            return (Registry<T>) BuiltInRegistries.ITEM;
        if (rawKey.equals(Registries.ENTITY_TYPE))
            return (Registry<T>) BuiltInRegistries.ENTITY_TYPE;
        if (rawKey.equals(Registries.BLOCK_ENTITY_TYPE))
            return (Registry<T>) BuiltInRegistries.BLOCK_ENTITY_TYPE;
        if (rawKey.equals(Registries.MENU))
            return (Registry<T>) BuiltInRegistries.MENU;
        if (rawKey.equals(Registries.SOUND_EVENT))
            return (Registry<T>) BuiltInRegistries.SOUND_EVENT;
        return null;
    }
}
