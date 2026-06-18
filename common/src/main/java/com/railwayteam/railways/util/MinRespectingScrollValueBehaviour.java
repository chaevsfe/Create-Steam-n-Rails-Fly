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

package com.railwayteam.railways.util;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.IntConsumer;

public class MinRespectingScrollValueBehaviour extends ScrollValueBehaviour {
    private int min;

    public MinRespectingScrollValueBehaviour(Component label, SmartBlockEntity be, ValueBoxTransform slot) {
        super(label, be, slot);
    }

    public ScrollValueBehaviour between(int min, int max) {
        this.min = min;
        return this;
    }

    public ScrollValueBehaviour setValue(int value) {
        return this;
    }

    public ScrollValueBehaviour withCallback(IntConsumer callback) {
        return this;
    }

    public ScrollValueBehaviour requiresWrench() {
        return this;
    }
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(this.label, 1, 1,
            ImmutableList.of(Component.literal("Value")),
            new ValueSettingsFormatter(vs -> Component.literal(Integer.toString(vs.value() + this.min))));
    }
    public void setValueSettings(Player player, ValueSettings vs, boolean ctrlDown) {
        super.setValueSettings(player, new ValueSettings(vs.row(), vs.value() + this.min), ctrlDown);
    }
    public ValueSettings getValueSettings() {
        ValueSettings vs = super.getValueSettings();
        return new ValueSettings(vs.row(), vs.value() - min);
    }
}
