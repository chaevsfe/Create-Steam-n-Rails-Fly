package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class CRAdvancements implements DataProvider {
    public static final AdvancementRef STRANGE_TEA = new AdvancementRef(Railways.asResource("strange_tea"));

    public CRAdvancements(PackOutput output) {
    }
    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.completedFuture(null);
    }
    public String getName() {
        return "Steam 'n' Rails' Advancements";
    }

    public static void provideLang(BiConsumer<String, String> consumer) {
    }

    public static void register() {
    }

    public static class AdvancementRef {
        private final Identifier id;

        private AdvancementRef(Identifier id) {
            this.id = id;
        }

        public void awardTo(ServerPlayer player) {
            AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
            if (advancement == null) {
                Railways.LOGGER.warn("Could not award missing advancement {}", id);
                return;
            }

            advancement.value().criteria().keySet().forEach(criterion ->
                player.getAdvancements().award(advancement, criterion));
        }
    }
}
