package com.railwayteam.railways.shim.create.foundation.data;

import com.railwayteam.railways.shim.registrate.builders.BlockBuilder;
import com.railwayteam.railways.shim.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.level.block.Block;

public class TagGen {
    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> axeOrPickaxe() {
        return NonNullUnaryOperator.identity();
    }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> pickaxeOnly() {
        return NonNullUnaryOperator.identity();
    }
}
