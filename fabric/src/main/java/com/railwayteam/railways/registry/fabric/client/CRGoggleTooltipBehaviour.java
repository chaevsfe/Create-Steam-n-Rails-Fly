/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.registry.fabric.client;

import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CRGoggleTooltipBehaviour extends TooltipBehaviour<SmartBlockEntity> implements IHaveGoggleInformation {
    private final BlockEntity source;

    public CRGoggleTooltipBehaviour(SmartBlockEntity blockEntity) {
        this(blockEntity, blockEntity);
    }

    private CRGoggleTooltipBehaviour(@Nullable SmartBlockEntity blockEntity, BlockEntity source) {
        super(blockEntity);
        this.source = source;
    }

    public static CRGoggleTooltipBehaviour detached(BlockEntity source) {
        return new CRGoggleTooltipBehaviour(null, source);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!(source instanceof IHaveGoggleInformation goggles))
            return false;
        int before = tooltip.size();
        return goggles.addToGoggleTooltip(tooltip, isPlayerSneaking) && tooltip.size() > before;
    }
}
