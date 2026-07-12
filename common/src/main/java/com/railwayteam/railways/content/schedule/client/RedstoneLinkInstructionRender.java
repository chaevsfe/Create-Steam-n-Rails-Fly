/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.schedule.client;

import com.google.common.collect.ImmutableList;
import com.railwayteam.railways.content.schedule.RedstoneLinkInstruction;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.content.trains.schedule.IScheduleInput;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Client-side editor and summary renderer for a wireless redstone pulse. */
public class RedstoneLinkInstructionRender implements IScheduleInput<RedstoneLinkInstruction> {
    @Override
    public int slotsTargeted() {
        return 2;
    }

    @Override
    public Pair<ItemStack, Component> getSummary(RedstoneLinkInstruction input) {
        return Pair.of(icon(), formatted(input));
    }

    private MutableComponent formatted(RedstoneLinkInstruction input) {
        return Component.translatable(
            "railways.schedule.instruction.redstone_link.power",
            input.intData("Power")
        );
    }

    @Override
    public List<Component> getSecondLineTooltip(int slot) {
        return ImmutableList.of(CreateLang.translateDirect(
            slot == 0 ? "logistics.firstFrequency" : "logistics.secondFrequency"
        ).withStyle(ChatFormatting.RED));
    }

    @Override
    public List<Component> getTitleAs(RedstoneLinkInstruction input, String type) {
        return ImmutableList.of(
            CreateLang.translateDirect("schedule.condition.redstone_link.frequency_powered"),
            Component.literal(" #1 ").withStyle(ChatFormatting.GRAY)
                .append(input.freq.getFirst().getStack().getHoverName().copy().withStyle(ChatFormatting.DARK_AQUA)),
            Component.literal(" #2 ").withStyle(ChatFormatting.GRAY)
                .append(input.freq.getSecond().getStack().getHoverName().copy().withStyle(ChatFormatting.DARK_AQUA))
        );
    }

    @Override
    public void setItem(RedstoneLinkInstruction input, int slot, ItemStack stack) {
        input.freq.set(slot == 0, Frequency.of(stack));
    }

    @Override
    public ItemStack getItem(RedstoneLinkInstruction input, int slot) {
        return input.freq.get(slot == 0).getStack();
    }

    @Override
    public ItemStack getSecondLineIcon() {
        return icon();
    }

    @Override
    public void initConfigurationWidgets(RedstoneLinkInstruction input, ModularGuiLineBuilder builder) {
        builder.addScrollInput(20, 101, (scroll, label) -> scroll
            .withRange(1, 16)
            .withStepFunction(context -> context.shift ? 5 : 1)
            .titled(Component.translatable("railways.schedule.instruction.redstone_link.power_edit_box")), "Power");
    }

    private ItemStack icon() {
        return AllItems.REDSTONE_LINK.getDefaultInstance();
    }
}
