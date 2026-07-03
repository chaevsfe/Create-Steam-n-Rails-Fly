/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

package com.railwayteam.railways.fabric_mixin.client;

import com.google.common.collect.ImmutableList;
import com.railwayteam.railways.registry.CRIcons;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter.ScrollOptionSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.RollingModeScrollBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.content.contraptions.actors.roller.RollerBlockEntity.RollingMode;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = RollingModeScrollBehaviour.class, remap = false)
public abstract class MixinRollingModeScrollBehaviour extends ScrollOptionBehaviour<RollingMode> {
    private static final INamedIconOptions[] RAILWAYS$ROLLING_MODE_OPTIONS = RollingModeOption.values();

    private MixinRollingModeScrollBehaviour() {
        super(null, null, null, (SmartBlockEntity) null, (ValueBoxTransform) null);
    }

    @Override
    public INamedIconOptions getIconForSelected() {
        return RAILWAYS$ROLLING_MODE_OPTIONS[Math.min(behaviour.getValue(), RAILWAYS$ROLLING_MODE_OPTIONS.length - 1)];
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(
            label,
            behaviour.getMax(),
            1,
            ImmutableList.of(Component.literal("Select")),
            new ScrollOptionSettingsFormatter(RAILWAYS$ROLLING_MODE_OPTIONS)
        );
    }

    private enum RollingModeOption implements INamedIconOptions {
        TUNNEL_PAVE(AllIcons.I_ROLLER_PAVE, "create.contraptions.roller_mode.tunnel_pave"),
        STRAIGHT_FILL(AllIcons.I_ROLLER_FILL, "create.contraptions.roller_mode.straight_fill"),
        WIDE_FILL(AllIcons.I_ROLLER_WIDE_FILL, "create.contraptions.roller_mode.wide_fill"),
        TRACK_REPLACE(CRIcons.I_SWAP_TRACKS, "create.contraptions.roller_mode.track_replace");

        private final AllIcons icon;
        private final String translationKey;

        RollingModeOption(AllIcons icon, String translationKey) {
            this.icon = icon;
            this.translationKey = translationKey;
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }
    }
}
