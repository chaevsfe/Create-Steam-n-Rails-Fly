package com.railwayteam.railways.fabric_mixin.client;

import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityVisual;
import com.zurrtum.create.client.content.trains.bogey.BogeyVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BogeyBlockEntityVisual creates its {@code bogey} visual after calling super(), so any
 * {@code updateLight} invoked by the flywheel infrastructure during construction sees a null bogey
 * and skips the light update. This leaves block-placed bogeys dark until the next light change event.
 * Fix: explicitly call updateLight after the bogey is assigned.
 */
@Mixin(value = BogeyBlockEntityVisual.class, remap = false)
public abstract class MixinBogeyBlockEntityVisual {
    @Shadow @Nullable private BogeyVisual bogey;

    @Shadow public abstract void updateLight(float partialTick);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void railways$initLight(VisualizationContext ctx, AbstractBogeyBlockEntity blockEntity, float partialTick, CallbackInfo ci) {
        if (bogey != null)
            updateLight(partialTick);
    }
}
