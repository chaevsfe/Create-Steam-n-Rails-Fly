/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */

package com.railwayteam.railways.content.buffer;

import com.railwayteam.railways.Railways;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.catnip.render.StitchedSprite;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.UnaryOperator;

@Environment(EnvType.CLIENT)
public class BufferModelUtils {
    public static final StitchedSprite SPRUCE_PLANKS_TEMPLATE =
        new StitchedSprite(Identifier.withDefaultNamespace("block/spruce_planks"));
    public static final StitchedSprite BIG_BUFFER_TEMPLATE =
        new StitchedSprite(Railways.asResource("block/buffer/big_buffer"));
    public static final StitchedSprite SMALL_BUFFER_TEMPLATE =
        new StitchedSprite(Railways.asResource("block/buffer/small_buffer"));
    public static final StitchedSprite SMALL_BUFFER_MONORAIL_TEMPLATE =
        new StitchedSprite(Railways.asResource("block/buffer/small_buffer_monorail"));
    public static final EnumMap<DyeColor, StitchedSprite> BIG_BUFFER_COLORS = new EnumMap<>(DyeColor.class);
    public static final EnumMap<DyeColor, StitchedSprite> SMALL_BUFFER_COLORS = new EnumMap<>(DyeColor.class);

    static {
        for (DyeColor color : DyeColor.values()) {
            BIG_BUFFER_COLORS.put(color, new StitchedSprite(
                Railways.asResource("block/buffer/big_buffer/big_buffer_" + color.getName())
            ));
            SMALL_BUFFER_COLORS.put(color, new StitchedSprite(
                Railways.asResource("block/buffer/small_buffer/small_buffer_" + color.getName())
            ));
        }
    }

    public static UnaryOperator<TextureAtlasSprite> getSwapper(@Nullable BlockState planksState) {
        if (planksState == null)
            return sprite -> null;

        Block planksBlock = planksState.getBlock();
        Identifier id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
        if (!id.getPath().endsWith("_planks"))
            return sprite -> null;

        return sprite -> sprite == SPRUCE_PLANKS_TEMPLATE.get()
            ? getSpriteOnSide(planksState, Direction.UP)
            : null;
    }

    public static UnaryOperator<TextureAtlasSprite> getSwapper(@Nullable DyeColor color) {
        if (color == null)
            return sprite -> null;

        return sprite -> {
            if (sprite == SMALL_BUFFER_TEMPLATE.get() || sprite == SMALL_BUFFER_MONORAIL_TEMPLATE.get())
                return SMALL_BUFFER_COLORS.get(color).get();
            if (sprite == BIG_BUFFER_TEMPLATE.get())
                return BIG_BUFFER_COLORS.get(color).get();
            return null;
        };
    }

    @SafeVarargs
    public static UnaryOperator<TextureAtlasSprite> combineSwappers(
        @Nullable UnaryOperator<TextureAtlasSprite>... swappers
    ) {
        return sprite -> {
            if (swappers == null)
                return null;
            for (UnaryOperator<TextureAtlasSprite> swapper : swappers) {
                if (swapper == null)
                    continue;
                TextureAtlasSprite replacement = swapper.apply(sprite);
                if (replacement != null)
                    return replacement;
            }
            return null;
        };
    }

    private static TextureAtlasSprite getSpriteOnSide(BlockState state, Direction side) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        RandomSource random = RandomSource.create(42L);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(random, parts);

        for (BlockStateModelPart part : parts) {
            List<BakedQuad> quads = part.getQuads(side);
            if (!quads.isEmpty())
                return quads.getFirst().materialInfo().sprite();
        }

        for (BlockStateModelPart part : parts) {
            for (BakedQuad quad : part.getQuads(null)) {
                if (quad.direction() == side)
                    return quad.materialInfo().sprite();
            }
        }

        return model.particleMaterial().sprite();
    }

    /** Ensures the static sprite registrations above are initialized before the first model reload. */
    public static void register() {
    }
}
