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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.TravellingPoint;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(value = AbstractBogeyBlock.class, remap = false)
public abstract class MixinAbstractBogeyBlock {
	@Unique
	private final ThreadLocal<Identifier> railways$supportTrackType = new ThreadLocal<>();

	@Inject(method = "isOnIncompatibleTrack", at = @At("HEAD"), cancellable = true)
	private void railways$compareTrackGauge(Carriage carriage, boolean leading,
											 CallbackInfoReturnable<Boolean> cir) {
		TravellingPoint point = leading ? carriage.getLeadingPoint() : carriage.getTrailingPoint();
		if (point.edge == null) {
			cir.setReturnValue(false);
			return;
		}

		CarriageBogey bogey = leading ? carriage.leadingBogey() : carriage.trailingBogey();
		BogeyStyle style = bogey.getStyle();
		Identifier trackType = CRTrackMaterials.getType(point.edge.getTrackMaterial());
		AbstractBogeyBlock<?> bogeyBlock = (AbstractBogeyBlock<?>) (Object) this;
		var validTypes = bogeyBlock.getValidPathfindingTypes(style);
		cir.setReturnValue(!trackType.equals(CRTrackMaterials.CRTrackType.UNIVERSAL)
			&& !validTypes.contains(trackType)
			&& !validTypes.contains(CRTrackMaterials.CRTrackType.UNIVERSAL));
	}

	@WrapMethod(
		method = "getNextStyle(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;"
	)
	private BogeyStyle railways$filterContextualStyleCycle(Level level, BlockPos pos,
														 Operation<BogeyStyle> original) {
		BlockState state = level.getBlockState(pos);
		return railways$withSupportTrack(level, pos, state, () -> original.call(level, pos));
	}

	@WrapMethod(
		method = "useItemOn(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"
	)
	private InteractionResult railways$filterWrenchStyleCycle(ItemStack stack, BlockState state, Level level,
														 BlockPos pos, Player player, InteractionHand hand,
														 BlockHitResult hit, Operation<InteractionResult> original) {
		return railways$withSupportTrack(
			level,
			pos,
			state,
			() -> original.call(stack, state, level, pos, player, hand, hit)
		);
	}

	@WrapOperation(
		method = "getNextStyle(Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;)Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;",
		at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;")
	)
	private Collection<BogeyStyle> railways$filterStyles(Map<Identifier, BogeyStyle> cycleGroup,
														 Operation<Collection<BogeyStyle>> original) {
		Collection<BogeyStyle> styles = original.call(cycleGroup);
		Identifier trackType = railways$supportTrackType.get();
		return trackType == null ? styles : CRBogeyStyles.filterStylesForTrack(styles, trackType);
	}

	@WrapOperation(
		method = "getNextStyle(Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;)Lcom/zurrtum/create/content/trains/bogey/BogeyStyle;",
		at = @At(
			value = "INVOKE",
			target = "Lcom/zurrtum/create/catnip/data/Iterate;cycleValue(Ljava/util/List;Ljava/lang/Object;)Ljava/lang/Object;"
		)
	)
	private Object railways$cycleFromFirstCompatibleStyle(List<Object> styles, Object current,
														 Operation<Object> original) {
		try {
			return original.call(styles, current);
		} catch (IllegalArgumentException ignored) {
			return styles.getFirst();
		}
	}

	@Unique
	private <T> T railways$withSupportTrack(Level level, BlockPos bogeyPos, BlockState bogeyState,
												  java.util.function.Supplier<T> action) {
		Identifier previous = railways$supportTrackType.get();
		Identifier current = railways$getSupportTrackType(level, bogeyPos, bogeyState);
		if (current == null)
			railways$supportTrackType.remove();
		else
			railways$supportTrackType.set(current);

		try {
			return action.get();
		} finally {
			if (previous == null)
				railways$supportTrackType.remove();
			else
				railways$supportTrackType.set(previous);
		}
	}

	@Unique
	private Identifier railways$getSupportTrackType(Level level, BlockPos bogeyPos, BlockState bogeyState) {
		AbstractBogeyBlock<?> bogeyBlock = (AbstractBogeyBlock<?>) (Object) this;
		BlockPos trackPos = bogeyBlock.isUpsideDown(bogeyState) ? bogeyPos.above() : bogeyPos.below();
		if (level.getBlockState(trackPos).getBlock() instanceof ITrackBlock trackBlock)
			return CRTrackMaterials.getType(trackBlock.getMaterial());
		return null;
	}
}
