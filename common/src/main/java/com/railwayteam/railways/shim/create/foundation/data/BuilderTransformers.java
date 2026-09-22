package com.railwayteam.railways.shim.create.foundation.data;

import com.railwayteam.railways.shim.registrate.builders.BlockBuilder;
import com.railwayteam.railways.shim.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;

public class BuilderTransformers {
    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> copycat() {
        return b -> b.initialProperties(SharedProperties::softMetal)
            .properties(p -> p.noOcclusion()
                .mapColor(MapColor.NONE)
                .isValidSpawn((state, level, pos, type) -> false))
            .transform(TagGen.axeOrPickaxe());
    }
}
