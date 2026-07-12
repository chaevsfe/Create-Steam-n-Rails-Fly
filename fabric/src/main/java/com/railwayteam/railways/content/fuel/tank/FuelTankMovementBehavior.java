package com.railwayteam.railways.content.fuel.tank;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Keeps the mounted tank's lerped fluid level ticking on the client. */
public class FuelTankMovementBehavior extends MovementBehaviour {
    @Override
    public boolean mustTickWhileDisabled() {
        return true;
    }

    @Override
    public void tick(MovementContext context) {
        if (!context.world.isClientSide()) {
            return;
        }
        BlockEntity be = AllClientHandle.INSTANCE.getBlockEntityClientSide(context.contraption, context.localPos);
        if (be instanceof FuelTankBlockEntity tank && tank.getFluidLevel() != null) {
            tank.getFluidLevel().tickChaser();
        }
    }
}
