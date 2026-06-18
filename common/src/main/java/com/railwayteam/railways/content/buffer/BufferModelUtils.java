package com.railwayteam.railways.content.buffer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

@Environment(EnvType.CLIENT)
public class BufferModelUtils {
	public static UnaryOperator<TextureAtlasSprite> getSwapper(@Nullable BlockState planksState) {
		return sprite -> null;
	}

	public static UnaryOperator<TextureAtlasSprite> getSwapper(@Nullable DyeColor color) {
		return sprite -> null;
	}

	@SafeVarargs
	public static UnaryOperator<TextureAtlasSprite> combineSwappers(@Nullable UnaryOperator<TextureAtlasSprite>... swappers) {
		return sprite -> {
			if (swappers == null)
				return null;
			for (UnaryOperator<TextureAtlasSprite> swapper : swappers) {
				if (swapper == null)
					continue;
				TextureAtlasSprite result = swapper.apply(sprite);
				if (result != null)
					return result;
			}
			return null;
		};
	}

	public static void register() {
	}
}
