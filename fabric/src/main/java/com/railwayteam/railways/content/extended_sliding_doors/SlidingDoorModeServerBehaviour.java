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

import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import net.minecraft.world.level.storage.ValueInput;

public final class SlidingDoorModeServerBehaviour extends ServerScrollOptionBehaviour<SlidingDoorMode> {
    public SlidingDoorModeServerBehaviour(SlidingDoorBlockEntity blockEntity) {
        super(SlidingDoorMode.class, blockEntity);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        value = Math.min(options.length - 1, Math.max(0, value));
    }
}
