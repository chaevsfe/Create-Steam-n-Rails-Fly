package com.railwayteam.railways.content.switches.fabric;

import com.railwayteam.railways.content.switches.TrackSwitchBlock;
import com.railwayteam.railways.shim.create.AllTags;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TrackSwitchUseOverride {
    public static void register() {
        UseBlockCallback.EVENT.register(TrackSwitchUseOverride::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator() || !player.isSecondaryUseActive())
            return InteractionResult.PASS;
        if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty())
            return InteractionResult.PASS;

        BlockState state = level.getBlockState(hit.getBlockPos());
        if (!(state.getBlock() instanceof TrackSwitchBlock))
            return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);
        if (AllTags.AllItemTags.WRENCH.matches(stack))
            return InteractionResult.PASS;

        InteractionResult result = state.useItemOn(stack, level, player, hand, hit);
        return result.consumesAction() ? result : InteractionResult.PASS;
    }
}
