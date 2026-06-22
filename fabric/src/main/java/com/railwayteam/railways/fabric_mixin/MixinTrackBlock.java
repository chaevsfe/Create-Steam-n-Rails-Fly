package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.bogey_menu.handler.BogeyMenuHandlerServer;
import com.railwayteam.railways.content.custom_tracks.CustomTrackBlock;
import com.railwayteam.railways.content.custom_tracks.casing.CasingCollisionUtils;
import com.railwayteam.railways.content.custom_tracks.monorail.MonorailTrackBlock;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRShapes;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

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

    @Inject(method = "getBogeyAnchor", at = @At("HEAD"), cancellable = true)
    private void railways$placeCustomStyle(BlockGetter world, BlockPos pos, BlockState state,
                                           CallbackInfoReturnable<BlockState> cir) {
        if (BogeyMenuHandlerServer.getCurrentPlayer() == null)
            return;

        var styleData = BogeyMenuHandlerServer.getStyle(BogeyMenuHandlerServer.getCurrentPlayer());
        BogeyStyle style = styleData.getFirst();
        Identifier trackType = CRTrackMaterials.getType(((TrackBlock) (Object) this).getMaterial());

        Optional<BogeyStyle> mappedStyleOptional = CRBogeyStyles.getMapped(style, trackType, true);
        if (mappedStyleOptional.isPresent())
            style = mappedStyleOptional.get();
        if (style == AllBogeyStyles.STANDARD)
            return;

        BogeySize size = styleData.getSecond() != null
            ? styleData.getSecond()
            : AllBogeySizes.allSortedIncreasing().get(0);
        int escape = AllBogeySizes.allSortedIncreasing().size();
        while (!style.validSizes().contains(size)) {
            if (escape-- <= 0)
                return;
            size = size.nextBySize();
        }

        Block block = style.getBlockForSize(size);
        cir.setReturnValue(block.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_AXIS,
                state.getValue(TrackBlock.SHAPE) == TrackShape.XO ? Direction.Axis.X : Direction.Axis.Z));
    }
}
