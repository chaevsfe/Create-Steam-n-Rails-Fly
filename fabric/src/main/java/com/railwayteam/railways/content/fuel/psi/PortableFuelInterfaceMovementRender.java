/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.psi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceBlock;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/** Moving-contraption renderer using Railway's fuel-interface partials. */
public final class PortableFuelInterfaceMovementRender implements MovementRenderBehaviour {
    @Override
    public ActorVisual createVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext movementContext
    ) {
        return new FuelInterfaceActorVisual(visualizationContext, simulationWorld, movementContext);
    }

    @Override
    public @Nullable MovementRenderState getRenderState(
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        PoseStack.Pose transform,
        Matrix4f worldMatrix4f
    ) {
        if (VisualizationManager.supportsVisualization(context.world)) {
            return null;
        }

        BlockPos pos = context.localPos;
        BlockState blockState = context.state;
        LerpedFloat animation = PortableStorageInterfaceMovement.getAnimation(context);
        Direction facing = blockState.getValue(PortableStorageInterfaceBlock.FACING);
        float yRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        float xRot = Mth.DEG_TO_RAD * (facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90);
        float topOffset = animation.getValue(AnimationTickHolder.getPartialTicks());
        float middleOffset = topOffset * 0.5f + 0.375f;
        int light = LightCoordsUtil.getLightCoords(renderWorld, pos);

        SuperByteBuffer middle = CachedBuffers.partial(middle(animation.settled()), blockState)
            .transform(transform).translate(pos).center().rotateY(yRot).rotateX(xRot).uncenter();
        SuperByteBuffer top = CachedBuffers.partial(CRBlockPartials.PORTABLE_FUEL_INTERFACE_TOP, blockState);
        SuperByteBuffer.copyTransform(middle, top);

        RenderState state = new RenderState();
        state.middle = middle.translate(0, middleOffset, 0).light(light).useLevelLight(context.world, worldMatrix4f)
            .extractRenderState();
        state.top = top.translate(0, topOffset, 0).light(light).useLevelLight(context.world, worldMatrix4f)
            .extractRenderState();
        return state;
    }

    private static com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel middle(boolean lit) {
        return lit
            ? CRBlockPartials.PORTABLE_FUEL_INTERFACE_MIDDLE_POWERED
            : CRBlockPartials.PORTABLE_FUEL_INTERFACE_MIDDLE;
    }

    private static final class RenderState implements MovementRenderState {
        private @UnknownNullability SuperByteBufferRenderState middle;
        private @UnknownNullability SuperByteBufferRenderState top;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            middle.submit(matrices, queue);
            top.submit(matrices, queue);
        }
    }

    private static final class FuelInterfaceActorVisual extends ActorVisual {
        private final FuelInterfaceInstance instance;

        private FuelInterfaceActorVisual(
            VisualizationContext context,
            VirtualRenderWorld world,
            MovementContext movementContext
        ) {
            super(context, world, movementContext);
            instance = new FuelInterfaceInstance(
                context.instancerProvider(),
                movementContext.state,
                movementContext.localPos,
                false
            );
            instance.middle.light(localBlockLight(), 0);
            instance.top.light(localBlockLight(), 0);
        }

        @Override
        public void beginFrame() {
            LerpedFloat animation = PortableStorageInterfaceMovement.getAnimation(context);
            instance.tick(animation.settled());
            instance.beginFrame(animation.getValue(AnimationTickHolder.getPartialTicks()));
        }

        @Override
        protected void _delete() {
            instance.remove();
        }
    }

    private static final class FuelInterfaceInstance {
        private final InstancerProvider instancerProvider;
        private final BlockState blockState;
        private final BlockPos instancePos;
        private final float angleX;
        private final float angleY;
        private boolean lit;
        private final TransformedInstance middle;
        private final TransformedInstance top;

        private FuelInterfaceInstance(
            InstancerProvider instancerProvider,
            BlockState blockState,
            BlockPos instancePos,
            boolean lit
        ) {
            this.instancerProvider = instancerProvider;
            this.blockState = blockState;
            this.instancePos = instancePos;
            Direction facing = blockState.getValue(PortableStorageInterfaceBlock.FACING);
            angleX = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
            angleY = AngleHelper.horizontalAngle(facing);
            this.lit = lit;

            middle = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(middle(lit)))
                .createInstance();
            top = instancerProvider.instancer(
                InstanceTypes.TRANSFORMED,
                Models.chunkPartial(CRBlockPartials.PORTABLE_FUEL_INTERFACE_TOP)
            ).createInstance();
        }

        private void beginFrame(float progress) {
            middle.setIdentityTransform().translate(instancePos).center().rotateYDegrees(angleY).rotateXDegrees(angleX)
                .uncenter().translate(0, progress * 0.5f + 0.375f, 0).setChanged();
            top.setIdentityTransform().translate(instancePos).center().rotateYDegrees(angleY).rotateXDegrees(angleX)
                .uncenter().translate(0, progress, 0).setChanged();
        }

        private void tick(boolean lit) {
            if (this.lit == lit) {
                return;
            }
            this.lit = lit;
            instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(middle(lit)))
                .stealInstance(middle);
        }

        private void remove() {
            middle.delete();
            top.delete();
        }

        @SuppressWarnings("unused")
        private void collectCrumblingInstances(Consumer<Instance> consumer) {
            consumer.accept(middle);
            consumer.accept(top);
        }
    }
}
