package com.railwayteam.railways.fabric_mixin.client;

import com.zurrtum.create.client.content.trains.track.TrackRenderer;
import com.zurrtum.create.content.trains.track.BezierConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = TrackRenderer.SegmentAngles.class, remap = false)
public interface InvokerSegmentAngles {
    @Invoker("<init>")
    static TrackRenderer.SegmentAngles railways$newSegmentAngles(BezierConnection connection) {
        throw new AssertionError();
    }
}
