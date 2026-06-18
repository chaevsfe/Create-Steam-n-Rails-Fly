/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.fabric;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.content.conductor.IConductorHoldingFakePlayer;
import com.zurrtum.create.infrastructure.player.FakePlayerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

// NOTICE: changes must be replicated in ConductorFakePlayerForge.
// annoying that we can't merge them any further.
public class ConductorFakePlayerFabric extends FakePlayerEntity implements IConductorHoldingFakePlayer {
	private final WeakReference<ConductorEntity> conductor;

	public ConductorFakePlayerFabric(ServerLevel world, ConductorEntity conductor) {
		super(world, ConductorEntity.FAKE_PLAYER_PROFILE);
		this.conductor = new WeakReference<>(conductor);
	}

	@Override
	@NotNull
	public Component getDisplayName() {
		return Component.translatable(Railways.MOD_ID + "." + "conductor_name");
	}

	@Override
	public EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
		return super.getDefaultDimensions(pose).withEyeHeight(0);
	}

	@Override
	public float getCurrentItemAttackStrengthDelay() {
		return 1 / 64f;
	}

	@Override
	public boolean canEat(boolean ignoreHunger) {
		return false;
	}

	@Override
	public @Nullable ConductorEntity getConductor() {
		return conductor.get();
	}
}
