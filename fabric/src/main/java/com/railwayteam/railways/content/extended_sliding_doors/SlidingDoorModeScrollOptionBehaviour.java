/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

package com.railwayteam.railways.content.extended_sliding_doors;

import com.railwayteam.railways.registry.CRIcons;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SlidingDoorModeScrollOptionBehaviour extends ScrollOptionBehaviour<SlidingDoorMode> {
    private static final DoorModeOption[] OPTIONS = DoorModeOption.values();

    public SlidingDoorModeScrollOptionBehaviour(SlidingDoorBlockEntity blockEntity) {
        super(
            DoorModeOption.class,
            mode -> OPTIONS[mode.ordinal()],
            Component.translatable("create.sliding_door.mode"),
            blockEntity,
            new SlidingDoorValueBoxTransform()
        );
        needsWrench = true;
    }

    public static SlidingDoorModeScrollOptionBehaviour create(SmartBlockEntity blockEntity) {
        return new SlidingDoorModeScrollOptionBehaviour((SlidingDoorBlockEntity) blockEntity);
    }

    private enum DoorModeOption implements INamedIconOptions {
        NORMAL(CRIcons.I_DOOR_NORMAL),
        MANUAL(CRIcons.I_DOOR_MANUAL),
        SPECIAL(CRIcons.I_DOOR_SPECIAL),
        SPECIAL_INVERTED(CRIcons.I_DOOR_SPECIAL_INVERTED);

        private final AllIcons icon;

        DoorModeOption(AllIcons icon) {
            this.icon = icon;
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return SlidingDoorMode.valueOf(name()).getTranslationKey();
        }
    }

    private static class SlidingDoorValueBoxTransform extends CenteredSideValueBoxTransform {
        SlidingDoorValueBoxTransform() {
            super((state, direction) -> {
                Direction facing = state.getValue(SlidingDoorBlock.FACING);
                boolean showAtAll = !state.getValue(SlidingDoorBlock.OPEN);
                return showAtAll && (direction == facing || direction == facing.getOpposite());
            });
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16);
        }

        @Override
        public Vec3 getLocalOffset(BlockState state) {
            Vec3 location = VecHelper.voxelSpace(
                8,
                8,
                state.getValue(SlidingDoorBlock.FACING) == direction ? 3 : 16
            );
            location = VecHelper.rotateCentered(
                location,
                AngleHelper.horizontalAngle(getSide()),
                Direction.Axis.Y
            );
            return VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
        }
    }
}
