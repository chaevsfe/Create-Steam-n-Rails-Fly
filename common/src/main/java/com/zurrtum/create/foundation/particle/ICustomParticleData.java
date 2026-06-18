package com.zurrtum.create.foundation.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface ICustomParticleData<T extends ParticleOptions> {
    interface Deserializer<T extends ParticleOptions> {
        T fromCommand(ParticleType<T> type, StringReader reader) throws CommandSyntaxException;

        T fromNetwork(ParticleType<T> type, FriendlyByteBuf buffer);
    }

    Deserializer<T> getDeserializer();

    MapCodec<T> getCodec(ParticleType<T> type);

    void writeToNetwork(FriendlyByteBuf buffer);

    default ParticleType<T> createType() {
        ICustomParticleData<T> self = this;
        return new ParticleType<T>(false) {
            public MapCodec<T> codec() {
                return self.getCodec(this);
            }

            @SuppressWarnings("unchecked")
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return StreamCodec.of(
                    (buf, value) -> ((ICustomParticleData<T>) value).writeToNetwork((FriendlyByteBuf) buf),
                    buf -> self.getDeserializer().fromNetwork(this, (FriendlyByteBuf) buf)
                );
            }
        };
    }
}
