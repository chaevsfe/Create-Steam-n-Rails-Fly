/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.animated_flywheel;

import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.mixin_interfaces.IDistanceTravelled;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Client-ticked actor behaviour which derives a flywheel's angle from the distance travelled by
 * its carriage. Rendering is attached separately on the physical client so this class remains
 * safe to load on a dedicated server.
 */
public final class FlywheelMovementBehaviour extends MovementBehaviour {
    public static final FlywheelMovementBehaviour INSTANCE = new FlywheelMovementBehaviour();

    static final double WHEEL_RADIUS = 1.4375;
    private static final double WHEEL_CIRCUMFERENCE = 2 * Math.PI * WHEEL_RADIUS;

    private FlywheelMovementBehaviour() {
    }

    @Override
    public boolean isActive(MovementContext context) {
        return context.world.isClientSide();
    }

    @Override
    public void tick(MovementContext context) {
        Object previousData = context.temporaryData;
        context.temporaryData = null;

        if (!isAnimationEnabled())
            return;
        if (!(context.contraption instanceof CarriageContraption carriageContraption))
            return;
        if (!(carriageContraption.entity instanceof CarriageContraptionEntity carriageEntity))
            return;

        Direction.Axis flywheelAxis = context.state.getValue(BlockStateProperties.AXIS);
        Direction assemblyDirection = carriageContraption.getAssemblyDirection();
        if (!canRotate(assemblyDirection, flywheelAxis))
            return;

        AngleData data = previousData instanceof AngleData angleData ? angleData : new AngleData();
        context.temporaryData = data;

        double distanceTravelled = ((IDistanceTravelled) carriageEntity).railways$getDistanceTravelled();
        float currentAngle = Float.isNaN(data.nextAngle) ? 0 : data.nextAngle;
        data.previousAngle = Mth.positiveModulo(currentAngle, 360);
        data.nextAngle = (float) (data.previousAngle + angleDelta(distanceTravelled, assemblyDirection));
    }

    /** The actor renderer replaces the virtual flywheel block entity in both rendering backends. */
    @Override
    public boolean disableBlockEntityRendering() {
        return true;
    }

    public static float getAngle(MovementContext context, float partialTick) {
        if (!isAnimationEnabled() || !(context.temporaryData instanceof AngleData data))
            return 0;
        return Mth.lerp(partialTick, data.previousAngle, data.nextAngle);
    }

    public static boolean isAnimationEnabled() {
        // Config registration finishes before a client level can tick, but treating a missing config
        // as the default value keeps early renderer construction deterministic.
        return CRConfigs.client() == null || CRConfigs.client().animatedFlywheels.get();
    }

    static boolean canRotate(Direction assemblyDirection, Direction.Axis flywheelAxis) {
        return !flywheelAxis.isVertical() && assemblyDirection.getAxis() != flywheelAxis;
    }

    static double angleDelta(double distanceTravelled, Direction assemblyDirection) {
        double angle = 360 * distanceTravelled / WHEEL_CIRCUMFERENCE;
        return assemblyDirection == Direction.SOUTH || assemblyDirection == Direction.WEST ? -angle : angle;
    }

    /** Fail-fast checks for the upstream wheel geometry and direction convention. */
    public static void verifyMath() {
        double fullTurn = angleDelta(WHEEL_CIRCUMFERENCE, Direction.NORTH);
        double reverseTurn = angleDelta(WHEEL_CIRCUMFERENCE, Direction.SOUTH);
        if (Math.abs(fullTurn - 360) > 1.0e-6 || Math.abs(reverseTurn + 360) > 1.0e-6)
            throw new IllegalStateException("Animated flywheel distance-to-angle conversion is invalid");
        if (!canRotate(Direction.NORTH, Direction.Axis.X)
            || canRotate(Direction.NORTH, Direction.Axis.Z)
            || canRotate(Direction.NORTH, Direction.Axis.Y))
            throw new IllegalStateException("Animated flywheel carriage-axis filtering is invalid");
    }

    private static final class AngleData {
        private float previousAngle = Float.NaN;
        private float nextAngle = Float.NaN;
    }
}
