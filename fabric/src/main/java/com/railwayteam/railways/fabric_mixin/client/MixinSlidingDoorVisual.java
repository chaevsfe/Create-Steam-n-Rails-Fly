package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorBlock;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(
    targets = {
        "com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorVisual$FoldingVisual",
        "com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorVisual$SlidingVisual"
    },
    remap = false
)
public abstract class MixinSlidingDoorVisual {
    @WrapOperation(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object railways$locometalDoorPartials(@SuppressWarnings("rawtypes") Map partials, Object key, Operation<Object> original,
                                                  @Local(argsOnly = true) SlidingDoorBlockEntity blockEntity) {
        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof PalettesSlidingDoorBlock door) {
            boolean windowed = state.getValue(PalettesSlidingDoorBlock.WINDOWED);
            if (door.isFoldingDoor())
                return CRBlockPartials.FOLDING_DOORS.get(door.color).get(windowed);
            return CRBlockPartials.SLIDING_DOORS.get(door.color).get(windowed);
        }
        return original.call(partials, key);
    }
}
