package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.railwayteam.railways.registry.CRBlockEntities;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = CopycatBlockEntity.class, remap = false)
public class MixinCopycatBlockEntity {
    @ModifyArgs(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/foundation/blockEntity/SmartBlockEntity;<init>(Lnet/minecraft/world/level/block/entity/BlockEntityType;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
        ),
        require = 0
    )
    private static void railways$useCopycatHeadstockType(Args args) {
        BlockState state = args.get(2);
        if (state.getBlock() instanceof CopycatHeadstockBlock)
            args.set(0, CRBlockEntities.COPYCAT_HEADSTOCK.get());
    }
}
