/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.conductor.remote_lens;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.util.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.UUID;

public class RemoteLensItem extends Item {
	private static final String SELECTED_CONDUCTOR = "SelectedConductor";

	public RemoteLensItem(Properties properties) {
		super(properties);
	}

	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		Optional<UUID> selectedConductor = getSelectedConductor(stack);
		if (selectedConductor.isPresent()) {
			UUID conductorId = selectedConductor.get();
			tooltip.accept(Component.translatable("railways.whistle.tool.bound").withStyle(ChatFormatting.DARK_GREEN));
			tooltip.accept(TextUtils.translateWithFormatting("railways.whistle.tool.conductor_id", conductorId.toString().substring(0, 5)));
			tooltip.accept(Component.translatable("railways.remote_lens.tool.bound_usage"));
		} else {
			tooltip.accept(Component.translatable("railways.remote_lens.tool.not_bound").withStyle(ChatFormatting.DARK_RED));
		}
	}

	public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player,
														   @NotNull LivingEntity target, @NotNull InteractionHand hand) {
		if (player.level().isClientSide())
			return InteractionResult.CONSUME;
		if (target instanceof ConductorEntity conductor && conductor.getJob() == ConductorEntity.Job.SPY) {
			setSelectedConductor(stack, conductor.getUUID());
			player.setItemInHand(hand, stack);
			player.displayClientMessage(Component.translatable("railways.remote_lens.set"), true);
			player.level().playSound(null, conductor.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, .5f, 1.1f);
			return InteractionResult.SUCCESS;
		}
		return super.interactLivingEntity(stack, player, target, hand);
	}

	public @NotNull InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() == null)
			return InteractionResult.FAIL;
		return use(context.getLevel(), context.getPlayer(), context.getHand());
	}

	public @NotNull InteractionResult use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide())
			return InteractionResult.CONSUME;
		if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer))
			return InteractionResult.FAIL;

		Optional<UUID> selectedConductor = getSelectedConductor(stack);
		if (selectedConductor.isEmpty())
			return InteractionResult.FAIL;

		if (player.isShiftKeyDown()) {
			clearSelectedConductor(stack);
			player.displayClientMessage(Component.translatable("railways.remote_lens.clear"), true);
			level.playSound(null, player.blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, .5f, 1.1f);
			return InteractionResult.SUCCESS;
		}

		Entity entity = serverLevel.getEntity(selectedConductor.get());
		if (entity instanceof ConductorEntity conductor && conductor.getJob() == ConductorEntity.Job.SPY)
			return conductor.startViewing(serverPlayer) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
		return InteractionResult.FAIL;
	}

	private static Optional<UUID> getSelectedConductor(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (!tag.contains(SELECTED_CONDUCTOR))
			return Optional.empty();
		try {
			return Optional.of(UUID.fromString(tag.getStringOr(SELECTED_CONDUCTOR, "")));
		} catch (IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}

	private static void setSelectedConductor(ItemStack stack, UUID conductorId) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(SELECTED_CONDUCTOR, conductorId.toString()));
	}

	private static void clearSelectedConductor(ItemStack stack) {
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(SELECTED_CONDUCTOR));
	}
}
