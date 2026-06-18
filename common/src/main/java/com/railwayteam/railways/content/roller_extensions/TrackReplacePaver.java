package com.railwayteam.railways.content.roller_extensions;

import com.zurrtum.create.content.contraptions.actors.roller.PaveTask;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.logistics.filter.FilterItemStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class TrackReplacePaver {
    @ApiStatus.Internal
    public static boolean tickInstantly;

    public static void pave(MovementContext context, BlockPos pos, BlockState stateToPaveWith, @Nullable PaveTask trackProfile) {
    }

    public static ItemStack extract(FilterItemStack filter, MovementContext context) {
        return extract(filter, context, 1);
    }

    @ExpectPlatform
    public static ItemStack extract(FilterItemStack filter, MovementContext context, int amt) {
        throw new AssertionError();
    }
}
