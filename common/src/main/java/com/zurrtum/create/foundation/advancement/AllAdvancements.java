package com.zurrtum.create.foundation.advancement;

import net.minecraft.server.level.ServerPlayer;

public class AllAdvancements {
    public static final AdvancementRef TRAIN_CRASH = new AdvancementRef();
    public static final AdvancementRef TRAIN_CRASH_BACKWARDS = new AdvancementRef();

    public static class AdvancementRef {
        public void awardTo(ServerPlayer player) {
        }
    }
}
