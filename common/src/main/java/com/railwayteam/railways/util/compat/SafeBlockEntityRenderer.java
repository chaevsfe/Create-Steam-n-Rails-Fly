package com.railwayteam.railways.util.compat;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public abstract class SafeBlockEntityRenderer<T extends SmartBlockEntity> extends SmartBlockEntityRenderer<T> {
    protected SafeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
}
