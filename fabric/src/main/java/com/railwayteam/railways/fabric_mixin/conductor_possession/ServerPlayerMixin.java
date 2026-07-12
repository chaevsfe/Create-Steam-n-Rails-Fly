package com.railwayteam.railways.fabric_mixin.conductor_possession;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, priority = 1200)
public class ServerPlayerMixin {
    /**
     * Vanilla spectator cameras snap the server-side player entity to the camera every tick. A
     * possessed conductor deliberately keeps the player's body where possession started, so only
     * the camera/chunk-tracking origin must move. The 26.2 method is {@code absSnapTo}, replacing
     * the older {@code absMoveTo}/{@code snapTo} call targeted by upstream Railway.
     */
    @Redirect(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;absSnapTo(DDDFF)V"
        )
    )
    private void railways$keepPlayerBodyAtPossessionOrigin(ServerPlayer player, double x, double y, double z,
                                                            float yaw, float pitch) {
        if (!(player.getCamera() instanceof ConductorEntity))
            player.absSnapTo(x, y, z, yaw, pitch);
    }

    @Inject(method = "setCamera", at = @At("HEAD"), cancellable = true)
    private void railways$keepConductorCamera(Entity entityToSpectate, CallbackInfo ci) {
        if (entityToSpectate instanceof ConductorEntity)
            ci.cancel();
    }
}
