/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.util.packet;

import com.railwayteam.railways.content.bogey_menu.handler.BogeyMenuHandlerServer;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BogeyStyleSelectionPacket implements C2SPacket {
    private final BogeyStyle style;
    @Nullable
    private final BogeySize size;

    public BogeyStyleSelectionPacket(@NotNull BogeyStyle style) {
        this(style, null);
    }

    public BogeyStyleSelectionPacket(@NotNull BogeyStyle style, @Nullable BogeySize size) {
        this.style = style;
        this.size = size;
    }

    public BogeyStyleSelectionPacket(FriendlyByteBuf buf) {
        Identifier styleId = buf.readIdentifier();
        style = AllBogeyStyles.BOGEY_STYLES.getOrDefault(styleId, AllBogeyStyles.STANDARD);
        Identifier sizeId = buf.readBoolean() ? buf.readIdentifier() : null;
        size = sizeId == null ? null : AllBogeySizes.all().get(sizeId);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeIdentifier(style.id);
        buffer.writeBoolean(size != null);
        if (size != null)
            buffer.writeIdentifier(size.id());
    }

    public void handle(ServerPlayer sender) {
        BogeyMenuHandlerServer.addStyle(sender.getUUID(), Pair.of(style, size));
    }
}
