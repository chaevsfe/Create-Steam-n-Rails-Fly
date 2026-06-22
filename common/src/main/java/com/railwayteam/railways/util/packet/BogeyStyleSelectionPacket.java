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
