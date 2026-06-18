package net.minecraft.client.resources.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface BakedModel {
    List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random);

    default boolean useAmbientOcclusion() {
        return true;
    }

    default boolean isGui3d() {
        return true;
    }

    default boolean usesBlockLight() {
        return true;
    }

    default boolean isCustomRenderer() {
        return false;
    }

    TextureAtlasSprite getParticleIcon();

    default ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    default ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
