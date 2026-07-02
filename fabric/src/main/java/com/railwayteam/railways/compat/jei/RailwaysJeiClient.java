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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.compat.jei;

import com.railwayteam.railways.Railways;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.client.compat.jei.JeiClientPlugin;
import com.zurrtum.create.client.compat.jei.category.SequencedAssemblyCategory;
import com.zurrtum.create.client.foundation.gui.render.SawRenderState;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.press.PressingRecipe;
import com.zurrtum.create.content.kinetics.saw.CuttingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.List;
import java.util.Optional;

public class RailwaysJeiClient implements IModPlugin {
	public static void register() {
		SequencedAssemblyCategory.registerRenderer(AllRecipeTypes.CUTTING, new CuttingSequencedAssemblyRenderer());
	}

	@Override
	public Identifier getPluginUid() {
		return Railways.asResource("jei_plugin");
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		List<RecipeHolder<PressingRecipe>> pressingRecipes = jeiRuntime.getRecipeManager()
			.createRecipeLookup(JeiClientPlugin.PRESSING)
			.includeHidden()
			.get()
			.toList();

		List<RecipeHolder<PressingRecipe>> hiddenPressingRecipes = pressingRecipes.stream()
			.filter(RailwaysJeiClient::isSequencedAssemblyTrackPressing)
			.toList();

		if (!hiddenPressingRecipes.isEmpty()) {
			jeiRuntime.getRecipeManager().hideRecipes(JeiClientPlugin.PRESSING, hiddenPressingRecipes);
			Railways.LOGGER.info("Hidden {} track sequenced assembly pressing recipes from JEI", hiddenPressingRecipes.size());
		}
	}

	private static boolean isSequencedAssemblyTrackPressing(RecipeHolder<PressingRecipe> recipeHolder) {
		Identifier recipeId = recipeHolder.id().identifier();
		return "create".equals(recipeId.getNamespace())
			&& recipeId.getPath().startsWith("sequenced_assembly_")
			&& recipeHolder.value().results().stream()
			.map(output -> BuiltInRegistries.ITEM.getKey(output.create().getItem()))
			.anyMatch(RailwaysJeiClient::isRailwaysTrack);
	}

	private static boolean isRailwaysTrack(Identifier id) {
		if (!Railways.MOD_ID.equals(id.getNamespace()))
			return false;

		return id.getPath().startsWith("track_");
	}

	private static class CuttingSequencedAssemblyRenderer extends SequencedAssemblyCategory.SequencedRenderer<CuttingRecipe> {
		@Override
		public void render(GuiGraphics graphics, int i, int x, int y, Optional<IRecipeSlotView> slot) {
			float scale = 19 / 33f;
			Matrix3x2fStack matrices = graphics.pose();
			matrices.pushMatrix();
			matrices.translate(x, y);
			matrices.scale(scale, scale);
			matrices.translate(-x, -y);
			graphics.guiRenderState.submitPicturesInPictureState(new SawRenderState(
				new Matrix3x2f(matrices),
				x - 3,
				y + 90
			));
			matrices.popMatrix();
		}

		@Override
		public Component getSequenceName(CuttingRecipe recipe, Optional<IRecipeSlotView> slot) {
			return CreateLang.translateDirect("recipe.sawing");
		}

		@Override
		public IRecipeSlotBuilder addSlot(IRecipeLayoutBuilder builder, int x, int y, CuttingRecipe recipe) {
			return null;
		}
	}
}
