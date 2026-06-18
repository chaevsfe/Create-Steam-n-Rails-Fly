package net.createmod.ponder.api.level;

import com.zurrtum.create.client.catnip.outliner.Outliner;
import net.minecraft.client.multiplayer.ClientLevel;

public abstract class PonderLevel extends ClientLevel {
    public final Scene scene = new Scene();

    protected PonderLevel() {
        super(null, null, null, null, 0, 0, null, false, 0, 0);
    }

    public static class Scene {
        public Outliner getOutliner() {
            return null;
        }
    }
}
