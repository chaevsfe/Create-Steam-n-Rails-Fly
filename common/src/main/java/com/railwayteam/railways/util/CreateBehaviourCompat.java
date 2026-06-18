package com.railwayteam.railways.util;

import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.world.level.block.Block;

public class CreateBehaviourCompat {
    public static <B extends Block> NonNullConsumer<B> interactionBehaviour(MovingInteractionBehaviour behaviour) {
        return block -> MovingInteractionBehaviour.REGISTRY.register(block, behaviour);
    }

    public static <B extends Block> NonNullConsumer<B> movementBehaviour(MovementBehaviour behaviour) {
        return block -> MovementBehaviour.REGISTRY.register(block, behaviour);
    }

    public static <B extends Block> NonNullConsumer<B> displaySource(RegistryEntry<? extends DisplaySource> source) {
        return block -> DisplaySource.BY_BLOCK.add(block, source.get());
    }

    public static <B extends Block> NonNullConsumer<B> displayTarget(RegistryEntry<? extends DisplayTarget> target) {
        return block -> DisplayTarget.BY_BLOCK.register(block, target.get());
    }
}
