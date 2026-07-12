/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.animated_flywheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.PalettesFlywheelBlock;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRPalettes;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KINETIC_BLOCK;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.shaft;

/** Client rendering for flywheels mounted on contraptions. */
public final class FlywheelMovementRender implements MovementRenderBehaviour {
    public static void register() {
        FlywheelMovementBehaviour.INSTANCE.attachRender = new FlywheelMovementRender();
        if (!(FlywheelMovementBehaviour.INSTANCE.getAttachRender() instanceof FlywheelMovementRender))
            throw new IllegalStateException("Animated flywheel movement render was not attached");

        for (PalettesColor color : PalettesColor.values()) {
            BlockState state = CRPalettes.Styles.FLYWHEEL.get(color).get().defaultBlockState();
            if (getWheelModel(state) != CRBlockPartials.FLYWHEELS.get(color))
                throw new IllegalStateException("Wrong animated flywheel partial for palette color " + color);
        }
        Railways.LOGGER.info("Animated flywheel instance and fallback render paths registered");
    }

    /** Shared by the contraption renderer and the world-render mixins. */
    public static PartialModel getWheelModel(BlockState state) {
        if (state.getBlock() instanceof PalettesFlywheelBlock paletteFlywheel)
            return CRBlockPartials.FLYWHEELS.get(paletteFlywheel.getColor());
        return AllPartialModels.FLYWHEEL;
    }

    @Override
    public @Nullable ActorVisual createVisual(
        VisualizationContext visualizationContext,
        VirtualRenderWorld simulationWorld,
        MovementContext movementContext
    ) {
        return new FlywheelActorVisual(visualizationContext, simulationWorld, movementContext);
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
        if (VisualizationManager.supportsVisualization(context.world))
            return null;

        BlockPos pos = context.localPos;
        BlockState blockState = context.state;
        Direction.Axis axis = blockState.getValue(BlockStateProperties.AXIS);
        Direction direction = axis.getPositive();
        int light = LightCoordsUtil.getLightCoords(renderWorld, pos);

        float shaftAngle = Mth.DEG_TO_RAD * KineticBlockEntityVisual.rotationOffset(blockState, axis, pos);
        SuperByteBuffer shaftBuffer = CachedBuffers.block(KINETIC_BLOCK, shaft(axis))
            .transform(transform)
            .translate(pos)
            .rotateCentered(shaftAngle, direction);

        float wheelAngle = FlywheelMovementBehaviour.getAngle(context, AnimationTickHolder.getPartialTicks());
        SuperByteBuffer wheelBuffer = CachedBuffers.partialFacingVertical(getWheelModel(blockState), blockState, direction)
            .transform(transform)
            .translate(pos)
            .rotateCentered(Mth.DEG_TO_RAD * wheelAngle, direction);

        FlywheelMovementRenderState state = new FlywheelMovementRenderState();
        state.shaft = shaftBuffer.light(light).useLevelLight(context.world, worldMatrix4f).extractRenderState();
        state.wheel = wheelBuffer.light(light).useLevelLight(context.world, worldMatrix4f).extractRenderState();
        return state;
    }

    private static final class FlywheelActorVisual extends ActorVisual {
        private final RotatingInstance shaft;
        private final TransformedInstance wheel;
        private final Matrix4f baseTransform = new Matrix4f();
        private float lastAngle = Float.NaN;

        private FlywheelActorVisual(
            VisualizationContext visualizationContext,
            VirtualRenderWorld simulationWorld,
            MovementContext movementContext
        ) {
            super(visualizationContext, simulationWorld, movementContext);

            BlockState state = movementContext.state;
            BlockPos pos = movementContext.localPos;
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
            Direction align = axis.getPositive();
            int blockLight = localBlockLight();

            shaft = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
                .createInstance();
            shaft.setRotationAxis(axis)
                .setRotationOffset(KineticBlockEntityVisual.rotationOffset(state, axis, pos))
                .setRotationalSpeed(0)
                .setPosition(pos)
                .rotateToFace(axis)
                .light(blockLight, 0)
                .setChanged();

            wheel = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(getWheelModel(state)))
                .createInstance();
            wheel.translate(pos)
                .center()
                .rotate(new Quaternionf().rotateTo(0, 1, 0, align.getStepX(), align.getStepY(), align.getStepZ()))
                .light(blockLight, 0);
            baseTransform.set(wheel.pose);
            animate(FlywheelMovementBehaviour.getAngle(movementContext, 0));
        }

        @Override
        public void beginFrame() {
            float angle = FlywheelMovementBehaviour.getAngle(context, AnimationTickHolder.getPartialTicks());
            if (Math.abs(angle - lastAngle) < 0.001f)
                return;
            animate(angle);
        }

        private void animate(float angle) {
            wheel.setTransform(baseTransform)
                .rotateY(AngleHelper.rad(angle))
                .uncenter()
                .setChanged();
            lastAngle = angle;
        }

        @Override
        protected void _delete() {
            shaft.delete();
            wheel.delete();
        }
    }

    private static final class FlywheelMovementRenderState implements MovementRenderState {
        private @UnknownNullability SuperByteBufferRenderState shaft;
        private @UnknownNullability SuperByteBufferRenderState wheel;

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            shaft.submit(matrices, queue);
            wheel.submit(matrices, queue);
        }
    }
}
