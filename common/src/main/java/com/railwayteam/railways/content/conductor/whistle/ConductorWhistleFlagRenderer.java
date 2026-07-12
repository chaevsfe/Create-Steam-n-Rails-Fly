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

package com.railwayteam.railways.content.conductor.whistle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ConductorWhistleFlagRenderer
	extends SmartBlockEntityRenderer<ConductorWhistleFlagBlockEntity, ConductorWhistleFlagRenderer.FlagRenderState> {

	public ConductorWhistleFlagRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public FlagRenderState createRenderState() {
		return new FlagRenderState();
	}

	@Override
	public void extractRenderState(ConductorWhistleFlagBlockEntity be, FlagRenderState state,
								   float tickProgress, Vec3 cameraPos,
								   @Nullable net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumbling) {
		super.extractRenderState(be, state, tickProgress, cameraPos, crumbling);
		if (be.isRemoved())
			return;

		state.flag = CachedBuffers.partial(CRBlockPartials.CONDUCTOR_WHISTLE_FLAGS.get(be.getColor()),
			Blocks.AIR.defaultBlockState());
	}

	@Override
	public void submit(FlagRenderState state, PoseStack matrices, SubmitNodeCollector queue,
					   CameraRenderState cameraState) {
		super.submit(state, matrices, queue, cameraState);

		if (state.flag != null) {
			queue.submitCustomGeometry(matrices, RenderTypes.cutoutMovingBlock(),
				(pose, consumer) -> state.flag
					.light(state.lightCoords)
					.renderInto(pose, consumer));
		}
	}

	public static class FlagRenderState extends SmartBlockEntityRenderer.SmartRenderState {
		public @Nullable SuperByteBuffer flag;
	}
}
