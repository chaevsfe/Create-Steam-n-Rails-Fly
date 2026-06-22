package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.events.ClientEvents;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void railways$onKeyInput(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (action == 0) return; // GLFW_RELEASE — only fire on press/repeat
        ClientEvents.onKeyInput(event.key(), true);
    }
}
