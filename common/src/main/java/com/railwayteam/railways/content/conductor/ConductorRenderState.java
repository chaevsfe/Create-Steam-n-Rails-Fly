package com.railwayteam.railways.content.conductor;

import com.railwayteam.railways.Railways;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ConductorRenderState extends LivingEntityRenderState {
    public DyeColor color = ConductorEntity.defaultColor();
    public ItemStack headStack = ItemStack.EMPTY;
    public ConductorEntity.Job job = ConductorEntity.Job.DEFAULT;
    public Identifier texture = Railways.asResource("textures/entity/conductor.png");

    // flag layer
    public boolean isHoldingSchedules = false;

    // toolbox layer
    public boolean isCarryingToolbox = false;
    public DyeColor toolboxColor = DyeColor.BROWN;
    public BlockState toolboxBlockState = null;
    public float toolboxLidAngle = 0f;
    public float toolboxDrawerOffset = 0f;
}
