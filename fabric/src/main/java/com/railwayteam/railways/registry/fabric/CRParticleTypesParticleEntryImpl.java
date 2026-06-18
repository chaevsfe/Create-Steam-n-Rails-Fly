package com.railwayteam.railways.registry.fabric;

import com.railwayteam.railways.Railways;
import com.zurrtum.create.foundation.particle.ICustomParticleData;
import com.zurrtum.create.foundation.particle.ICustomParticleDataWithSprite;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.function.Supplier;

public class CRParticleTypesParticleEntryImpl {
    public static void register(String id, Supplier<ParticleType<?>> supplier) {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Railways.asResource(id), supplier.get());
    }

    public static void register() {
        com.railwayteam.railways.registry.CRParticleTypes.init();
    }

    @SuppressWarnings("unchecked")
    @Environment(EnvType.CLIENT)
    public static <T extends ParticleOptions> void registerFactory(ParticleType<T> object, ParticleEngine engine, ICustomParticleData<T> customParticleData) {
        if (customParticleData instanceof ICustomParticleDataWithSprite<T> withSprite) {
            ParticleFactoryRegistry.getInstance().register(object, sprite -> withSprite.getMetaFactory().apply(sprite));
        }
    }
}
