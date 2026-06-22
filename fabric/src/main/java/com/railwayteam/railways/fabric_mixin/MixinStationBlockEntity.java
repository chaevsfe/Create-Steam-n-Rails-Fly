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
import java.util.Optional;
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

	@Inject(method = "trackClicked", at = @At("RETURN"))
	private void railways$clearPlayer(Player player, InteractionHand hand, ITrackBlock track, BlockState state,
									  BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		try {
			if (cir.getReturnValueZ())
				railways$applyStyleToNewBogeys(player, track, pos);
		} finally {
			BogeyMenuHandlerServer.setCurrentPlayer(null);
			railways$bogeysBeforeClick = Set.of();
		}
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
		BogeyStyle style = styleData.getFirst();
		Optional<BogeyStyle> mappedStyle = CRBogeyStyles.getMapped(style, trackType, true);
		if (mappedStyle.isPresent())
			style = mappedStyle.get();
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
