package com.railwayteam.railways.util.compat;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public abstract class SmartBlockEntityRenderer<T extends SmartBlockEntity>
    extends com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer<T,
    com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer.SmartRenderState> {
    protected SmartBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
