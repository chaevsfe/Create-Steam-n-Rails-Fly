package com.railwayteam.railways.util.fabric;

import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.packet.s2c.HonkReturnPacket;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.nio.file.Path;

public class UtilsImpl {
    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    public static void sendHonkPacket(Level level, Train train, boolean isHonk) {
        MinecraftServer server = level.getServer();
        if (server != null) {
            server.getPlayerList().broadcastAll(new HonkReturnPacket(train, isHonk));
        }
    }

    public static void postChunkEventClient(LevelChunk chunk, boolean load) {
    }

    public static Path modsDir() {
        return FabricLoader.getInstance().getGameDir().resolve("mods");
    }
}
