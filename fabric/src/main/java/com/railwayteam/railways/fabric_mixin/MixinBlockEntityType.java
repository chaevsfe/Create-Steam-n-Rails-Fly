package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.railwayteam.railways.content.conductor.vent.VentBlock;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.content.trains.track.TrackBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create's track block-entity type ({@code AllBlockEntityTypes.TRACK}) computes its valid-block set
 * from {@code TrackMaterial.allBlocks()} at Create's init time, before Railways registers its own
 * track materials, so Railways tracks (e.g. {@code railways:track_ender}) are missing from that set.
 * In 1.21 {@code BlockEntity.validateBlockState} strictly enforces the set, so creating a track
 * block-entity for a Railways track throws and crashes the server during track propagation (and
 * prevents curving). Railways tracks are plain Create {@link TrackBlock}s, so accept any TrackBlock
 * for the TRACK type.
 */
@Mixin(BlockEntityType.class)
public class MixinBlockEntityType {
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void railways$allowAddonTracks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == AllBlockEntityTypes.TRACK && state.getBlock() instanceof TrackBlock)
            cir.setReturnValue(true);
        if ((Object) this == AllBlockEntityTypes.COPYCAT && state.getBlock() instanceof CopycatHeadstockBlock)
            cir.setReturnValue(true);
        if ((Object) this == AllBlockEntityTypes.COPYCAT && state.getBlock() instanceof VentBlock)
            cir.setReturnValue(true);
    }
}
