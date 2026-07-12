package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorBlock;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorRenderer;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(SlidingDoorRenderer.class)
public class MixinSlidingDoorRenderer {
    @WrapOperation(
        method = "extractRenderState(Lcom/zurrtum/create/content/decoration/slidingDoor/SlidingDoorBlockEntity;Lcom/zurrtum/create/client/content/decoration/slidingDoor/SlidingDoorRenderer$DoorRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;")
    )
    private Object getPalettesPartials(@SuppressWarnings("rawtypes") Map instance, Object key, Operation<Object> original,
                                       @Local SlidingDoorBlock block,
                                       @Local(argsOnly = true) SlidingDoorBlockEntity blockEntity) {
        if (block instanceof PalettesSlidingDoorBlock paletteBlock && paletteBlock.isFoldingDoor()) {
            return CRBlockPartials.FOLDING_DOORS.get(paletteBlock.color)
                .get(blockEntity.getBlockState().getValue(PalettesSlidingDoorBlock.WINDOWED));
        }
        return original.call(instance, key);
    }
}
