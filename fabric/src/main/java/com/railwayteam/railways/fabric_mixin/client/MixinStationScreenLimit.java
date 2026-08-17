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

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.mixin_interfaces.ILimited;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.util.packet.StationLimitPacket;
import com.zurrtum.create.client.content.trains.station.AbstractStationScreen;
import com.zurrtum.create.client.content.trains.station.StationScreen;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StationScreen.class, remap = false)
public abstract class MixinStationScreenLimit extends AbstractStationScreen {
    private MixinStationScreenLimit(StationBlockEntity te, GlobalStation station) {
        super(te, station);
    }

    @Inject(
        method = "init",
        at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/client/content/trains/station/StationScreen;tickTrainDisplay()V")
    )
    private void railways$addLimitCheckbox(CallbackInfo ci) {
        Checkbox limitCheckbox = Checkbox.builder(Component.translatable("railways.station.train_limit"), font)
            .pos(guiLeft + background.getWidth() - 98, guiTop + background.getHeight() - 26)
            .selected(station != null && ((ILimited) station).isLimitEnabled())
            .tooltip(Tooltip.create(
                Component.translatable("railways.station.train_limit.tooltip.1"),
                Component.translatable("railways.station.train_limit.tooltip.2")
            ))
            .onValueChange((checkbox, selected) -> CRPackets.PACKETS.send(new StationLimitPacket(blockEntity.getBlockPos(), selected)))
            .build();
        addRenderableWidget(limitCheckbox);
    }
}
