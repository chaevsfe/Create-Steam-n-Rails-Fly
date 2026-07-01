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
import com.railwayteam.railways.content.smokestack.block.be.SmokeStackBlockEntity;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.catnip.lang.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

/**
 * See {@link MixinTrackCouplerBlockEntityGoggles} for why this bridge - and the whole method
 * body, not just the interface - has to live here rather than on SmokeStackBlockEntity itself.
 */
@Mixin(SmokeStackBlockEntity.class)
@Implements(@Interface(iface = IHaveGoggleInformation.class, prefix = "goggle$"))
public abstract class MixinSmokeStackBlockEntityGoggles {
    @Shadow
    protected @Nullable DyeColor color;

    public boolean goggle$addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        SmokeStackBlockEntity be = (SmokeStackBlockEntity) (Object) this;
        if (be.isSoul()) {
            Lang.builder(Railways.MOD_ID)
                .translate("smokestack.goggle.tooltip", Component.translatable("railways.smokestack.goggle.tooltip.style.soul"))
                .forGoggles(tooltip);
        } else {
            DyeColor color = this.color != null ? this.color : DyeColor.BLACK;
            Lang.builder(Railways.MOD_ID)
                .translate("smokestack.goggle.tooltip.color", Component.translatable("color.minecraft." + color.getName()))
                .forGoggles(tooltip);
        }

        return true;
    }
}
