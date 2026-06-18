/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.config;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.util.Utils;
import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class CRConfigs {

    @ApiStatus.Internal
    public static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    private static CClient client;
    private static CCommon common;
    private static CServer server;

    public static CClient client() {
        return client;
    }

    public static CCommon common() {
        return common;
    }

    public static CServer server() {
        return server;
    }

    public static ConfigBase byType(ModConfig.Type type) {
        return CONFIGS.get(type);
    }

    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        T config = Builder.create(factory, Railways.MOD_ID, side.name().toLowerCase());
        CONFIGS.put(side, config);
        return config;
    }

    @ApiStatus.Internal
    public static void registerCommon() {
        client = register(CClient::new, ModConfig.Type.CLIENT);
        common = register(CCommon::new, ModConfig.Type.COMMON);
        server = register(CServer::new, ModConfig.Type.SERVER);
    }

    public static void onLoad(ModConfig modConfig) {
    }

    public static void onReload(ModConfig modConfig) {
    }

    public static String migrateClient(String contents) {
        Map<String, String> m = new HashMap<>();
        m.put("conductorSpyShader", "useConductorSpyShader");
        m.put("extendedCouplerDebug", "showExtendedCouplerDebug");
        m.put("trackOverlayOffset", "trackOverlayOffset");
        m.put("skipClientsideDerailing", "skipClientDerailing");

        m.put("trainSmokePercentage", "smoke.smokePercentage");
        m.put("trainSmokeLifetime", "smoke.smokeLifetime");
        m.put("smokeTextureQuality", "smoke.smokeQuality");

        Map<String, String> trueMap = new HashMap<>();
        for (Map.Entry<String, String> entry : m.entrySet()) {
            trueMap.put("general."+entry.getKey(), "client."+entry.getValue());
        }
        return migrate(contents, trueMap);
    }

    public static String migrateCommon(String contents) {
        Map<String, String> m = new HashMap<>();
        m.put("registerMissingTracks", "registerMissingTracks");
        m.put("disableDatafixer", "disableDatafixer");

        Map<String, String> trueMap = new HashMap<>();
        for (Map.Entry<String, String> entry : m.entrySet()) {
            trueMap.put("general."+entry.getKey(), entry.getValue());
        }
        return migrate(contents, trueMap);
    }

    private static class TomlGroup {
        private final Map<String, TomlGroup> subgroups = new HashMap<>();
        private final Map<String, String> entries = new HashMap<>();
        private final String path;

        private static TomlGroup root() {
            return new TomlGroup("");
        }

        private TomlGroup(String path) {
            this.path = path;
        }

        public boolean isRoot() {
            return path.isEmpty();
        }

        public void add(String key, String value) {
            if (!isRoot())
                throw new NotImplementedException();

            String[] pieces = key.split("\\.");
            String subKey = pieces[pieces.length - 1];
            TomlGroup targetedGroup = this;
            for (int i = 0; i < pieces.length - 1; i++) {
                targetedGroup = targetedGroup.getOrCreateSubGroup(pieces[i]);
            }
            targetedGroup.entries.put(subKey, value);
        }

        private TomlGroup getOrCreateSubGroup(String subKey) {
            return subgroups.computeIfAbsent(subKey, (sk) -> new TomlGroup(path.isEmpty() ? sk : path + "." + sk));
        }

        private void write(StringBuilder b) {
            if (!isRoot()) {
                b.append("\n[").append(path).append("]");
            }

            for (Map.Entry<String, String> entry : entries.entrySet()) {
                b.append("\n").append(entry.getKey()).append(" = ").append(entry.getValue());
            }

            for (TomlGroup subGroup : subgroups.values()) {
                subGroup.write(b);
            }
        }

        private String write() {
            StringBuilder b = new StringBuilder();
            b.append("# Automatically written by a converter");
            write(b);
            b.append("\n");
            return b.toString();
        }
    }

    @ApiStatus.Internal
    public static String migrate(String contents, Map<String, String> pathMap) {
        TomlGroup root = TomlGroup.root();
        String keyPrefix = "";
        List<String> lines = contents.lines().toList();
        for (String line : lines) {
            if (line.isEmpty())
                continue;
            int commentIdx = line.indexOf('#');
            if (commentIdx != -1)
                line = line.substring(0, commentIdx);
            line = line
                .trim()
                .replaceAll(" ", "");

            if (line.isEmpty())
                continue;

            if (line.startsWith("[") && line.endsWith("]")) {
                keyPrefix = line.substring(1, line.length() - 1) + ".";
            } else if (line.contains("=")) {
                String[] pieces = line.split("=", 2);
                String key = keyPrefix + pieces[0].trim();
                if (!pathMap.containsKey(key))
                    continue;
                root.add(pathMap.get(key), pieces[1].trim());
            }
        }

        return root.write();
    }

    private static void preloadValues() {
        cachedDisableDatafixer = false;
        cachedRegisterMissingTracks = false;
    }


    private static Boolean cachedDisableDatafixer;
    private static Boolean cachedRegisterMissingTracks;
    public static boolean getDisableDatafixer() {
        if (common != null)
            return common.disableDatafixer.get();

        if (cachedDisableDatafixer == null) {
            preloadValues();
        }

        return cachedDisableDatafixer;
    }

    public static boolean getRegisterMissingTracks() {
        if (common != null)
            return common.registerMissingTracks.get();

        if (cachedRegisterMissingTracks == null) {
            preloadValues();
        }

        return cachedRegisterMissingTracks;
    }
}
