package com.railwayteam.railways.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.railwayteam.railways.Railways;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public enum CRKeys {
    BOGEY_MENU("bogey_menu", GLFW.GLFW_KEY_LEFT_ALT),
    CYCLE_MENU("cycle_menu", GLFW.GLFW_KEY_LEFT_ALT);

    public static final Set<KeyMapping> NON_CONFLICTING_KEYMAPPINGS = new HashSet<>();
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Railways.asResource("railways"));

    private final String description;
    private final int key;
    private KeyMapping keybind;

    CRKeys(String description, int defaultKey) {
        this.description = Railways.MOD_ID + ".keyinfo." + description;
        this.key = defaultKey;
    }

    public static void register() {
        for (CRKeys key : values()) {
            key.keybind = new KeyMapping(key.description, key.key, CATEGORY);
            NON_CONFLICTING_KEYMAPPINGS.add(key.keybind);
            registerKeyBinding(key.keybind);
        }
    }

    public static void fixBinds() {
        for (CRKeys key : values()) {
            if (key.keybind == null || key.keybind.isUnbound())
                continue;
            key.keybind.setDown(InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key.getBoundCode()));
        }
    }

    public KeyMapping getKeybind() {
        return keybind;
    }

    public boolean isPressed() {
        return keybind != null && keybind.isDown();
    }

    public String getBoundKey() {
        return keybind == null ? "" : keybind.getTranslatedKeyMessage().getString().toUpperCase();
    }

    public int getBoundCode() {
        return keybind == null ? InputConstants.UNKNOWN.getValue() : getBoundCode(keybind);
    }

    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key);
    }

    public static boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().handle(), button) == GLFW.GLFW_PRESS;
    }

    public static boolean ctrlDown() {
        return isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) || isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    public static boolean shiftDown() {
        return isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static boolean altDown() {
        return isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) || isKeyDown(GLFW.GLFW_KEY_RIGHT_ALT);
    }

    @ExpectPlatform
    private static void registerKeyBinding(KeyMapping keyMapping) {
        throw new AssertionError();
    }

    @ExpectPlatform
    private static int getBoundCode(KeyMapping keyMapping) {
        throw new AssertionError();
    }
}
