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

package com.railwayteam.railways.content.smokestack.particles.legacy;

import com.railwayteam.railways.config.CRConfigs;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class SmokeParticle extends SimpleAnimatedParticle {

	public enum SmokeQuality {
		LOW("smoke_16"),
		MEDIUM("smoke_32"),
		HIGH("smoke_64"),
		ULTRA("smoke");

		public final String name;

		SmokeQuality(String name) {
			this.name = name;
		}
	}

	private final LerpedFloat ascendScale = LerpedFloat.linear().startWithValue(1.0);
	private final double baseYd;
	private final boolean stationarySource;

	protected SmokeParticle(ClientLevel world, SmokeParticleData data, double x, double y, double z, double dx, double dy, double dz, SpriteSet sprite) {
		super(world, x, y, z, sprite, world.random.nextFloat() * .5f);
		double scale = 0.1;
		xd = dx * scale;
		yd = dy * scale + (this.random.nextFloat() / 500.0F);
		baseYd = yd;
		zd = dz * scale;
		this.gravity = 3.0E-6f;
		quadSize = .375f * 4;
		setLifetime(data.stationary ? 400 : CRConfigs.client().smokeLifetime.get());
		setPos(x, y, z);
		roll = oRoll = world.random.nextFloat() * Mth.PI;
		this.setSprite(sprite.get(CRConfigs.client().smokeQuality.get().ordinal(), SmokeQuality.values().length - 1));
		alpha = data.stationary ? 0.25f : 0.1f;
		ascendScale.chase(data.stationary ? 0.3 : 0.1, data.stationary ? 0.001 : 0.03, LerpedFloat.Chaser.EXP);
		rCol = data.red;
		gCol = data.green;
		bCol = data.blue;
		stationarySource = data.stationary;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime || this.alpha <= 0.0f) {
			this.remove();
			return;
		}
		float diffusionScale = stationarySource ? 800.0f : 500.0f;
		if (this.age > 350) {
			diffusionScale = 5000.0f;
		} else if (this.age > 300) {
			diffusionScale = Mth.lerp((age - 300) / 50.0f, stationarySource ? 800.0f : 500.0f, 5000.0f);
		}
		this.xd += this.random.nextFloat() / diffusionScale * (this.random.nextBoolean() ? 1 : -1);
		this.zd += this.random.nextFloat() / diffusionScale * (this.random.nextBoolean() ? 1 : -1);
		this.yd = this.baseYd * ascendScale.getValue();
		ascendScale.tickChaser();
		this.move(this.xd, this.yd, this.zd);
		if (this.age >= this.lifetime - 100 && this.alpha > 0.01f) {
			this.alpha -= 0.015f;
		}
	}

	@Override
	public int getLightColor(float partialTick) {
		BlockPos blockPos = BlockPos.containing(this.x, this.y, this.z);
		return this.level.isLoaded(blockPos) ? LevelRenderer.getLightColor(level, blockPos) : 0;
	}

	public static class Factory implements ParticleProvider<SmokeParticleData> {
		private final SpriteSet spriteSet;

		public Factory(SpriteSet animatedSprite) {
			this.spriteSet = animatedSprite;
		}

		@Override
		public Particle createParticle(SmokeParticleData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			return new SmokeParticle(worldIn, data, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
		}
	}
}
