package com.zurrtum.create.foundation.data;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.zurrtum.create.client.foundation.block.connected.ConnectedTextureBehaviour;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class CreateRegistrate extends Registrate {
    private Function<Item, TooltipModifier> tooltipModifierFactory;
    private final Set<Item> tooltipItems = Collections.newSetFromMap(new IdentityHashMap<>());

    protected CreateRegistrate(String modid) {
        super(modid);
    }

    public static CreateRegistrate create(String modid) {
        return new CreateRegistrate(modid);
    }

    public static <T extends Block> NonNullConsumer<T> blockModel(Supplier<?> factory) {
        return NonNullConsumer.noop();
    }

    public static <T extends Block> NonNullConsumer<T> connectedTextures(Supplier<? extends ConnectedTextureBehaviour> behaviour) {
        return NonNullConsumer.noop();
    }

    public static <T extends Item> NonNullConsumer<T> itemModel(Supplier<?> factory) {
        return NonNullConsumer.noop();
    }

    public void setTooltipModifierFactory(Function<Item, TooltipModifier> factory) {
        this.tooltipModifierFactory = factory;
    }

    @Override
    public void registerTooltipModifier(Item item) {
        if (tooltipModifierFactory == null || !tooltipItems.add(item))
            return;
        TooltipModifier.REGISTRY.register(item, tooltipModifierFactory.apply(item));
    }

    public void setCreativeTab(ResourceKey<CreativeModeTab> tab) {
        setCurrentCreativeTab(tab);
    }

    public void register() {
    }
}
