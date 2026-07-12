/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.mixin_interfaces.IStandardBogeyTEVirtualCoupling;
import com.zurrtum.create.content.trains.bogey.StandardBogeyBlockEntity;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Supplies the transient virtual-coupler controls used by Railway's coupler Ponder scene. */
@Mixin(value = StandardBogeyBlockEntity.class, remap = false)
public class MixinStandardBogeyBlockEntity implements IStandardBogeyTEVirtualCoupling {
    @Unique
    private double railways$couplingDistance = -1;
    @Unique
    private Direction railways$couplingDirection = Direction.UP;
    @Unique
    private boolean railways$couplingFront;

    @Override
    public void setCouplingDistance(double distance) {
        railways$couplingDistance = Double.isFinite(distance) ? distance : -1;
    }

    @Override
    public double getCouplingDistance() {
        return railways$couplingDistance;
    }

    @Override
    public void setCouplingDirection(Direction direction) {
        railways$couplingDirection = direction == null ? Direction.UP : direction;
    }

    @Override
    public Direction getCouplingDirection() {
        return railways$couplingDirection;
    }

    @Override
    public void setFront(boolean front) {
        railways$couplingFront = front;
    }

    @Override
    public boolean getFront() {
        return railways$couplingFront;
    }
}
