package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.Railways;
import com.zurrtum.create.content.trains.track.TrackBlock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionCompiler.class)
public class MixinSectionCompiler {
    @WrapOperation(
        method = "compile",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/ItemBlockRenderTypes;getChunkRenderType(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;"
        )
    )
    private ChunkSectionLayer railways$useCutoutForRailwaysTracks(BlockState state, Operation<ChunkSectionLayer> original) {
        if (state.getBlock() instanceof TrackBlock trackBlock
            && Railways.MOD_ID.equals(trackBlock.getMaterial().getId().getNamespace())) {
            return ChunkSectionLayer.CUTOUT;
        }
        return original.call(state);
    }
}
