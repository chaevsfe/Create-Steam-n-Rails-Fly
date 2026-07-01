/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.switches.TrackSwitchBlockEntity;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

/**
 * See {@link MixinTrackCouplerBlockEntityGoggles} for why this bridge - and the whole method
 * body, not just the interface - has to live here rather than on TrackSwitchBlockEntity itself.
 */
@Mixin(TrackSwitchBlockEntity.class)
@Implements(@Interface(iface = IHaveGoggleInformation.class, prefix = "goggle$"))
public abstract class MixinTrackSwitchBlockEntityGoggles {
    private static LangBuilder b() {
        return Lang.builder(Railways.MOD_ID);
    }

    public boolean goggle$addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        TrackSwitchBlockEntity be = (TrackSwitchBlockEntity) (Object) this;
        b().translate("tooltip.switch.header").forGoggles(tooltip);
        b().translate("tooltip.switch.state")
                .style(ChatFormatting.YELLOW)
                .forGoggles(tooltip);
        b().translate("switch.state." + be.getState().getSerializedName())
                .style(ChatFormatting.YELLOW)
                .forGoggles(tooltip);

        return true;
    }
}
