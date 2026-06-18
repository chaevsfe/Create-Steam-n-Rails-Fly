package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.custom_tracks.CustomTrackBlock;
import com.railwayteam.railways.content.custom_tracks.casing.CasingCollisionUtils;
import com.railwayteam.railways.content.custom_tracks.monorail.MonorailTrackBlock;
import com.railwayteam.railways.registry.CRShapes;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TrackBlock.class, remap = false)
public class MixinTrackBlock {
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true, remap = true)
    private void railways$useCasing(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player,
                                    InteractionHand hand, BlockHitResult hit,
                                    CallbackInfoReturnable<InteractionResult> cir) {
        if ((Object) this instanceof MonorailTrackBlock)
            return;

        InteractionResult result = CustomTrackBlock.casingUse(state, world, pos, player, hand, hit);
        if (result != null)
            cir.setReturnValue(result);
    }

    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true, remap = true)
    private void railways$casingCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                               CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (level.getBlockEntity(pos) instanceof TrackBlockEntity tbe
            && CasingCollisionUtils.shouldMakeCollision(tbe, state)) {
            cir.setReturnValue(CRShapes.BOTTOM_SLAB);
        }
    }
}
