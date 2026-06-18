package com.railwayteam.railways.registry;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class CRAdvancements implements DataProvider {
    public static final AdvancementRef STRANGE_TEA = new AdvancementRef();

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
        public void awardTo(ServerPlayer player) {
        }
    }
}
