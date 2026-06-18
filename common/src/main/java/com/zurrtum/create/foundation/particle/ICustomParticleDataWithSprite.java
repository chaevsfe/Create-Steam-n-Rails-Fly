package com.zurrtum.create.foundation.particle;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;

import java.util.function.Function;

public interface ICustomParticleDataWithSprite<T extends ParticleOptions> extends ICustomParticleData<T> {
    Function<SpriteSet, ParticleProvider<T>> getMetaFactory();
}
