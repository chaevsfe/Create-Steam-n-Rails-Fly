package com.railwayteam.railways.content.fuel;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LiquidFuelManager {
    private static final Map<Identifier, LiquidFuelType> CUSTOM_TYPE_MAP = new HashMap<>();
    private static final Map<Fluid, LiquidFuelType> FLUID_TO_TYPE_MAP = new IdentityHashMap<>();
    private static final Map<TagKey<Fluid>, LiquidFuelType> TAG_TO_TYPE_MAP = new IdentityHashMap<>();

    public static void clear() {
        CUSTOM_TYPE_MAP.clear();
        FLUID_TO_TYPE_MAP.clear();
        TAG_TO_TYPE_MAP.clear();
    }

    public static LiquidFuelType getTypeForFluid(Fluid fluid) {
        return FLUID_TO_TYPE_MAP.get(fluid);
    }

    @Nullable
    public static LiquidFuelType isInTag(Fluid fluid) {
        for (Map.Entry<TagKey<Fluid>, LiquidFuelType> entry : TAG_TO_TYPE_MAP.entrySet()) {
            if (fluid.defaultFluidState().is(entry.getKey()))
                return entry.getValue();
        }
        return null;
    }

    public static void fillFluidMap() {
        for (LiquidFuelType type : CUSTOM_TYPE_MAP.values()) {
            for (Supplier<Fluid> delegate : type.getFluids()) {
                FLUID_TO_TYPE_MAP.put(delegate.get(), type);
            }
            for (Supplier<TagKey<Fluid>> delegate : type.getFluidTags()) {
                TAG_TO_TYPE_MAP.put(delegate.get(), type);
            }
        }
    }

    public static class ReloadListener implements ResourceManagerReloadListener {
        public static final ReloadListener INSTANCE = new ReloadListener();
        public static final String ID = "railways_liquid_fuel";
        private static final FileToIdConverter LISTER = FileToIdConverter.json(ID);

        protected ReloadListener() {
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            clear();

            LISTER.listMatchingResources(resourceManager).forEach((fileId, resource) -> {
                Identifier id = LISTER.fileToId(fileId);
                try (var reader = resource.openAsReader()) {
                    JsonElement element = JsonParser.parseReader(reader);
                    if (!element.isJsonObject())
                        return;

                    JsonObject object = element.getAsJsonObject();
                    LiquidFuelType type = LiquidFuelType.fromJson(object);
                    if (type != null)
                        CUSTOM_TYPE_MAP.put(id, type);
                } catch (IOException | RuntimeException ignored) {
                }
            });

            fillFluidMap();
        }
    }
}
