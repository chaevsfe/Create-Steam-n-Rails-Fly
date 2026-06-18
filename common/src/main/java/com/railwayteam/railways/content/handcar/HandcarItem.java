package com.railwayteam.railways.content.handcar;

import com.railwayteam.railways.mixin_interfaces.IDeployAnywayBlockItem;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem.OverlapResult;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class HandcarItem extends BlockItem implements IDeployAnywayBlockItem {
    public HandcarItem(Block block, Properties properties) {
        super(block, properties);
    }

    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Environment(EnvType.CLIENT)
    public boolean useOnCurve(TrackBlockOutline.BezierPointSelection selection, ItemStack stack) {
        return false;
    }

    public static void withGraphLocation(Level level, BlockPos pos, boolean front,
                                         BezierTrackPointLocation targetBezier,
                                         BiConsumer<OverlapResult, TrackGraphLocation> callback) {
        callback.accept(OverlapResult.NO_TRACK, null);
    }
}
