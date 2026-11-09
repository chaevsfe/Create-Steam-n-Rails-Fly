/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.railwayteam.railways.Railways;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import java.util.HashSet;
import java.util.Set;

public enum CRKeys {
    BOGEY_MENU("bogey_menu", InputConstants.KEY_LALT),
    CYCLE_MENU("cycle_menu", InputConstants.KEY_LALT);

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
        MouseHandler mouse = Minecraft.getInstance().mouseHandler;
        return switch (button) {
            case InputConstants.MOUSE_BUTTON_LEFT -> mouse.isLeftPressed();
            case InputConstants.MOUSE_BUTTON_RIGHT -> mouse.isRightPressed();
            case InputConstants.MOUSE_BUTTON_MIDDLE -> mouse.isMiddlePressed();
            default -> false;
        };
    }

    public static boolean ctrlDown() {
        return isKeyDown(InputConstants.KEY_LCONTROL) || isKeyDown(InputConstants.KEY_RCONTROL);
    }

    public static boolean shiftDown() {
        return isKeyDown(InputConstants.KEY_LSHIFT) || isKeyDown(InputConstants.KEY_RSHIFT);
    }

    public static boolean altDown() {
        return isKeyDown(InputConstants.KEY_LALT) || isKeyDown(InputConstants.KEY_RALT);
    }

    private static void registerKeyBinding(KeyMapping keyMapping) {
        com.railwayteam.railways.registry.fabric.CRKeysImpl.registerKeyBinding(keyMapping);
    }

    private static int getBoundCode(KeyMapping keyMapping) {
        return com.railwayteam.railways.registry.fabric.CRKeysImpl.getBoundCode(keyMapping);
    }
}
