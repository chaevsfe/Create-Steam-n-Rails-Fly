package com.tterrag.registrate.builders;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.client.renderer.RenderType;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockBuilder<T extends Block, P> extends AbstractBuilder<T, P, BlockBuilder<T, P>> {
    private final Function<BlockBehaviour.Properties, T> factory;
    private Function<BlockBehaviour.Properties, BlockBehaviour.Properties> properties = Function.identity();
    private boolean callbacksRun = false;
    private boolean itemCallbacksRun = false;

    public BlockBuilder(Registrate owner, String name, Function<BlockBehaviour.Properties, T> factory) {
        this(owner, name, factory, null);
    }

    public BlockBuilder(Registrate owner, String name, Function<BlockBehaviour.Properties, T> factory, P parent) {
        super(owner, name, parent);
        this.factory = factory;
    }

    public BlockBuilder<T, P> initialProperties(Supplier<?> supplier) {
        this.properties = ignored -> copyInitialProperties(supplier.get());
        return this;
    }

    public BlockBuilder<T, P> properties(Function<BlockBehaviour.Properties, BlockBehaviour.Properties> transformer) {
        this.properties = this.properties.andThen(transformer);
        return this;
    }

    public BlockBuilder<T, P> blockstate(NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> consumer) {
        return this;
    }

    public BlockBuilder<T, P> loot(NonNullBiConsumer<? super com.tterrag.registrate.providers.loot.RegistrateBlockLootTables, ? super T> consumer) {
        return this;
    }

    public BlockBuilder<T, P> addLayer(Supplier<? extends Supplier<RenderType>> layer) {
        return this;
    }

    public ItemBuilder<BlockItem, BlockBuilder<T, P>> item() {
        return item(BlockItem::new);
    }

    public <I extends BlockItem> ItemBuilder<I, BlockBuilder<T, P>> item(BiFunction<? super T, Item.Properties, ? extends I> factory) {
        return new ItemBuilder<I, BlockBuilder<T, P>>(owner, name, props -> factory.apply(getOrCreate(), props), this)
            .properties(Item.Properties::useBlockDescriptionPrefix);
    }

    public BlockBuilder<T, P> removeTag(ProviderType<?> type, TagKey<?> tag) {
        return this;
    }

    public BlockBuilder<T, P> simpleItem() {
        item().register();
        return this;
    }

    public BlockEntry<T> register() {
        T block = getOrCreate();
        if (!callbacksRun) {
            runRegisterCallbacks(block);
            callbacksRun = true;
        }
        if (!itemCallbacksRun) {
            runAfterRegisterCallbacks(Registries.ITEM, block);
            itemCallbacksRun = true;
        }
        return new BlockEntry<>(owner.id(name), block);
    }

    private T getOrCreate() {
        T existing = BuiltInRegistries.BLOCK.getOptional(owner.id(name)).map(block -> (T) block).orElse(null);
        if (existing != null)
            return existing;
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, owner.id(name));
        T block = factory.apply(properties.apply(BlockBehaviour.Properties.of()).setId(key));
        owner.registerVanilla(BuiltInRegistries.BLOCK, name, block);
        return block;
    }

    private static BlockBehaviour.Properties copyInitialProperties(Object supplied) {
        if (supplied instanceof BlockBehaviour.Properties properties)
            return properties;
        if (supplied instanceof BlockBehaviour behaviour) {
            try {
                var method = BlockBehaviour.Properties.class.getMethod("ofFullCopy", BlockBehaviour.class);
                return (BlockBehaviour.Properties) method.invoke(null, behaviour);
            } catch (ReflectiveOperationException ignored) {
                return BlockBehaviour.Properties.of();
            }
        }
        return BlockBehaviour.Properties.of();
    }
}
