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
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlockEntity;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Locale;

/**
 * IHaveGoggleInformation is a client-only Create Fly interface, and LangBuilder.forGoggles()
 * depends on Minecraft.getInstance().font for indentation - both genuinely client-only.
 * TrackCouplerBlockEntity is a common block entity that must load on the dedicated server, so
 * neither the interface nor this method body can live there directly: a class's own methods are
 * fully verified (and their referenced types resolved) the instant the class loads, on either
 * side, regardless of any runtime guard around invoking them. The whole tooltip body is moved
 * here instead of just bridging to a method on the target, and @Shadow gives access to the
 * target's private clientInfo field.
 */
@Mixin(TrackCouplerBlockEntity.class)
@Implements(@Interface(iface = IHaveGoggleInformation.class, prefix = "goggle$"))
public abstract class MixinTrackCouplerBlockEntityGoggles {
    @Shadow
    private TrackCouplerBlockEntity.ClientInfo clientInfo;

    @Shadow
    public abstract TrackCouplerBlockEntity.AllowedOperationMode getAllowedOperationMode();

    private static LangBuilder b() {
        return Lang.builder(Railways.MOD_ID);
    }

    public boolean goggle$addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        b().translate("tooltip.coupler.header").forGoggles(tooltip);
        b().translate("tooltip.coupler.mode")
                .style(ChatFormatting.YELLOW)
                .forGoggles(tooltip);
        b().translate("coupler.mode." + getAllowedOperationMode().getSerializedName())
                .style(ChatFormatting.YELLOW)
                .forGoggles(tooltip);

        String train1 = clientInfo == null ? "None" : clientInfo.trainName1;
        String train2 = clientInfo == null ? "None" : clientInfo.trainName2;
        TrackCouplerBlockEntity.OperationMode operationMode = clientInfo == null ? TrackCouplerBlockEntity.OperationMode.NONE : clientInfo.mode;
        b().translate("tooltip.coupler.train1", train1)
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip);
        b().translate("tooltip.coupler.train2", train2)
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip);

        b().translate("tooltip.coupler.action." + operationMode.name().toLowerCase(Locale.ROOT))
                .style(ChatFormatting.GREEN)
                .forGoggles(tooltip);
        if (clientInfo != null) {
            if (clientInfo.error != null) {
                b().add(clientInfo.error)
                        .style(ChatFormatting.DARK_RED)
                        .forGoggles(tooltip);
            }
            if (clientInfo.error2 != null && CRConfigs.client().showExtendedCouplerDebug.get()) {
                b().add(clientInfo.error2)
                        .style(ChatFormatting.DARK_PURPLE)
                        .forGoggles(tooltip);
            }
        }
        return true;
    }
}
