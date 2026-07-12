package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.content.bogey_menu.handler.BogeyMenuHandlerServer;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = StationBlockEntity.class, remap = false)
public class MixinStationBlockEntity {
	@Unique
	private Set<BlockPos> railways$bogeysBeforeClick = Set.of();

	@Inject(method = "trackClicked", at = @At("HEAD"))
	private void railways$storePlayer(Player player, InteractionHand hand, ITrackBlock track, BlockState state,
									  BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		BogeyMenuHandlerServer.setCurrentPlayer(player.getUUID());
		railways$bogeysBeforeClick = railways$collectBogeys(pos);
	}

	@Inject(
		method = "trackClicked(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lcom/zurrtum/create/content/trains/track/ITrackBlock;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Z",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z",
			ordinal = 0,
			shift = At.Shift.BEFORE,
			remap = true
		),
		cancellable = true,
		remap = false
	)
	private void railways$rejectIncompatibleBogey(Player player, InteractionHand hand, ITrackBlock track,
														BlockState state, BlockPos pos,
														CallbackInfoReturnable<Boolean> cir) {
		Identifier trackType = CRTrackMaterials.getType(track.getMaterial());
		if (trackType.equals(CRTrackMaterials.CRTrackType.MONORAIL)
			|| trackType.equals(CRTrackMaterials.CRTrackType.UNIVERSAL))
			return;

		var styleData = BogeyMenuHandlerServer.getStyle(player.getUUID());
		if (CRBogeyStyles.resolveForTrack(
			styleData.getFirst(), styleData.getSecond(), trackType, true
		).isPresent())
			return;

		player.sendOverlayMessage(Component.translatable("railways.bogey.wrong_gauge"));
		railways$clearPlacementContext();
		cir.setReturnValue(false);
	}

	@Inject(method = "trackClicked", at = @At("RETURN"))
	private void railways$clearPlayer(Player player, InteractionHand hand, ITrackBlock track, BlockState state,
									  BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		try {
			if (cir.getReturnValueZ())
				railways$applyStyleToNewBogeys(player, track, pos);
		} finally {
			railways$clearPlacementContext();
		}
	}

	@Unique
	private void railways$clearPlacementContext() {
		BogeyMenuHandlerServer.setCurrentPlayer(null);
		railways$bogeysBeforeClick = Set.of();
	}

	@Unique
	private Set<BlockPos> railways$collectBogeys(BlockPos center) {
		Level level = ((StationBlockEntity) (Object) this).getLevel();
		Set<BlockPos> result = new HashSet<>();
		if (level == null)
			return result;

		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -4, -4), center.offset(4, 4, 4))) {
			if (level.getBlockEntity(pos) instanceof AbstractBogeyBlockEntity)
				result.add(pos.immutable());
		}
		return result;
	}

	@Unique
	private void railways$applyStyleToNewBogeys(Player player, ITrackBlock track, BlockPos center) {
		Level level = ((StationBlockEntity) (Object) this).getLevel();
		if (level == null)
			return;

		Identifier trackType = CRTrackMaterials.getType(track.getMaterial());
		if (trackType.equals(CRTrackMaterials.CRTrackType.MONORAIL))
			return;

		var styleData = BogeyMenuHandlerServer.getStyle(player.getUUID());
		var resolved = CRBogeyStyles.resolveForTrack(
			styleData.getFirst(), styleData.getSecond(), trackType, true
		);
		if (resolved.isEmpty())
			return;
		BogeyStyle style = resolved.get().style();
		if (style == AllBogeyStyles.STANDARD)
			return;

		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -4, -4), center.offset(4, 4, 4))) {
			if (railways$bogeysBeforeClick.contains(pos))
				continue;
			if (level.getBlockEntity(pos) instanceof AbstractBogeyBlockEntity bogeyBE) {
				bogeyBE.setBogeyStyle(style);
			}
		}
	}
}
