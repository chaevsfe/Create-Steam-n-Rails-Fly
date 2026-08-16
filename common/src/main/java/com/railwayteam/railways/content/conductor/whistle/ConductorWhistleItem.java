/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
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

package com.railwayteam.railways.content.conductor.whistle;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRSounds;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.TextUtils;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSchedules;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.Create;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.deployer.DeployerFakePlayer;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.schedule.Schedule;
import com.zurrtum.create.content.trains.schedule.ScheduleEntry;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import com.zurrtum.create.content.trains.schedule.condition.ScheduledDelay;
import com.zurrtum.create.content.trains.schedule.destination.DestinationInstruction;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlock;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.content.trains.track.TrackTargetingBlockItem;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class ConductorWhistleItem extends TrackTargetingBlockItem {
	public static final String SPECIAL_MARKER = "<ConductorFlag>";

	public ConductorWhistleItem(Block block, Item.Properties properties) {
		super(block, properties, EdgePointType.STATION);
	}

	public boolean useOnCurve(TrackBlockOutline.BezierPointSelection selection, ItemStack stack) {
		return false;
	}

	private static InteractionResult fail(Player player, String message) {
		player.sendOverlayMessage(Component.translatable("railways.whistle.failure." + message)
			.withStyle(ChatFormatting.RED));
		player.sendSystemMessage(Component.translatable("railways.whistle.failure." + message)
			.withStyle(ChatFormatting.RED));
		return InteractionResult.FAIL;
	}

	@Override
	public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display,
								@NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
		super.appendHoverText(stack, context, display, tooltip, flag);
		CompoundTag tag = getStackData(stack);
		Optional<UUID> selectedTrain = getUuid(tag, "SelectedTrain");
		Optional<UUID> selectedConductor = getUuid(tag, "SelectedConductor");
		if (selectedTrain.isPresent() && selectedConductor.isPresent()) {
			UUID trainId = selectedTrain.get();
			UUID conductorId = selectedConductor.get();
			String trainName = "NOT FOUND";
			GlobalRailwayManager railways = Create.RAILWAYS;
			if (railways != null && railways.trains.containsKey(trainId))
				trainName = railways.trains.get(trainId).name.getString();

			tooltip.accept(Component.translatable("railways.whistle.tool.bound").withStyle(ChatFormatting.DARK_GREEN));
			tooltip.accept(TextUtils.translateWithFormatting("railways.whistle.tool.conductor_id", conductorId.toString().substring(0, 5)));
			tooltip.accept(TextUtils.translateWithFormatting("railways.whistle.tool.train_id", trainName, trainId.toString().substring(0, 5)));
			tooltip.accept(Component.translatable("railways.whistle.tool.bound_usage"));
			tooltip.accept(Component.translatable("railways.whistle.tool.bound_auto_usage"));
			tooltip.accept(Component.translatable("railways.whistle.tool.bound_auto_clear"));
		} else {
			tooltip.accept(Component.translatable("railways.whistle.tool.not_bound").withStyle(ChatFormatting.DARK_RED));
		}
	}

	@Override
	public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player,
														   @NotNull LivingEntity target, @NotNull InteractionHand hand) {
		if (player.level().isClientSide())
			return InteractionResult.PASS;

		if (target instanceof ConductorEntity conductor && conductor.getVehicle() instanceof CarriageContraptionEntity cce) {
			Train train = cce.getCarriage().train;
			if (train.owner == player.getUUID() || !CRConfigs.server().conductors.whistleRequiresOwning.get()) {
				CompoundTag stackTag = getStackData(stack);
				putUuid(stackTag, "SelectedTrain", train.id);
				putUuid(stackTag, "SelectedConductor", conductor.getUUID());
				stackTag.putByte("SelectedColor", conductor.getEntityData().get(ConductorEntity.COLOR));
				player.sendOverlayMessage(Component.translatable("railways.whistle.set"));
				setStackData(stack, stackTag);
				player.setItemInHand(hand, stack);
				AllSoundEvents.PECULIAR_BELL_USE.play(player.level(), null, conductor.getX(), conductor.getY(),
					conductor.getZ(), .5f, 1.1f);
				return InteractionResult.SUCCESS;
			}

			player.sendOverlayMessage(Component.translatable("railways.whistle.not_owner")
				.withStyle(ChatFormatting.RED));
			return InteractionResult.FAIL;
		}

		return super.interactLivingEntity(stack, player, target, hand);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getItemInHand();
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		BlockState state = level.getBlockState(pos);
		Player player = context.getPlayer();
		CompoundTag stackTag = getStackData(stack);
		Optional<UUID> selectedTrain = getUuid(stackTag, "SelectedTrain");
		if (selectedTrain.isEmpty())
			return InteractionResult.FAIL;

		UUID trainId = selectedTrain.get();
		Train train = Create.RAILWAYS.trains.get(trainId);
		if (player == null || train == null)
			return InteractionResult.FAIL;

		if (player instanceof DeployerFakePlayer && state.getBlock() instanceof AirBlock && train.runtime.isAutoSchedule)
			train.runtime.discardSchedule();

		if (player.isSteppingCarefully() && !getStackData(stack).isEmpty()) {
			if (level.isClientSide())
				return InteractionResult.SUCCESS;
			player.sendOverlayMessage(Component.translatable("railways.whistle.clear"));
			stack.remove(DataComponents.CUSTOM_DATA);
			AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, .5f);
			return InteractionResult.SUCCESS;
		}

		if (!(state.getBlock() instanceof StationBlock) && !(state.getBlock() instanceof ITrackBlock))
			return InteractionResult.FAIL;

		level.playSound(null, pos, CRSounds.CONDUCTOR_WHISTLE.get(), SoundSource.BLOCKS, 0.3f, 1f);
		if (level.isClientSide())
			return InteractionResult.SUCCESS;

		String stationName = "";
		BlockPos temporaryFlagPos = null;
		BlockPos temporaryTrackPos = null;
		boolean temporaryFlagDirection = false;
		byte selectedColor = stackTag.getByteOr("SelectedColor", (byte) 0);

		Optional<UUID> selectedConductor = getUuid(stackTag, "SelectedConductor");
		if (selectedConductor.isEmpty())
			return fail(player, "not_bound");

		UUID conductorId = selectedConductor.get();
		if (!Create.RAILWAYS.trains.containsKey(trainId))
			return fail(player, "train_missing");

		Carriage conductorCarriage = null;
		for (Carriage carriage : train.carriages) {
			if (carriageHasPassenger(carriage, conductorId)) {
				conductorCarriage = carriage;
				break;
			}
		}

		if (conductorCarriage == null) {
			return fail(player, "conductor_missing");
		}

		if (state.getBlock() instanceof ITrackBlock track) {
			Vec3 lookAngle = player.getLookAngle();
			boolean front = track.getNearestTrackAxis(level, pos, state, lookAngle)
				.getSecond() == Direction.AxisDirection.POSITIVE;

			stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS, pos);
			stack.set(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION, front);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_BEZIER);

			EdgePointType<?> type = getType(stack);
			MutableObject<OverlapResult> result = new MutableObject<>(null);
			withGraphLocation(level, pos, front, null, type, (overlap, location) -> result.setValue(overlap));

			if (result.getValue().feedback != null) {
				player.sendOverlayMessage(CreateLang.translateDirect(result.getValue().feedback)
					.withStyle(ChatFormatting.RED));
				AllSoundEvents.DENY.play(level, null, pos, .5f, 1);
				return InteractionResult.FAIL;
			}

			Direction[] directions = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};
			Direction successDirection = null;
			for (Direction direction : directions) {
				BlockPos placePos = pos.relative(direction);
				Vec3 hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
					.add(direction.getStepX() * 0.5, direction.getStepY() * 0.5, direction.getStepZ() * 0.5);
				BlockPlaceContext ctx = new BlockPlaceContext(
					player, context.getHand(), stack, new BlockHitResult(hitPos, direction.getOpposite(), placePos, false)
				);
				if (level.getBlockState(placePos).canBeReplaced(ctx)) {
					successDirection = direction;
					break;
				}
			}

			if (successDirection == null) {
				stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
				stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
				return fail(player, "no_space");
			}

			BlockPos placePos = pos.relative(successDirection);
			stationName = SPECIAL_MARKER + placePos.toShortString();
			temporaryFlagPos = placePos;
			temporaryTrackPos = pos;
			temporaryFlagDirection = front;

			loadWhistleFlag(level, player, train, placePos, pos, stationName, selectedColor, front);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
		} else if (level.getBlockEntity(pos) instanceof StationBlockEntity stationBe) {
			stationName = Objects.requireNonNull(stationBe.getStation()).name;
		}

		if (CRConfigs.server().conductors.whistleRequiresOwning.get() && train.runtime.getSchedule() != null
			&& !train.runtime.completed && !train.runtime.isAutoSchedule && train.getOwner(level) != player) {
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_POS);
			stack.remove(AllDataComponents.TRACK_TARGETING_ITEM_SELECTED_DIRECTION);
			return fail(player, "not_owner");
		}

		if (train.runtime.getSchedule() != null && !train.runtime.isAutoSchedule) {
			ItemStack scheduleStack = train.runtime.returnSchedule(player.registryAccess());
			if (!scheduleStack.isEmpty() && conductorCarriage != null) {
				conductorCarriage.forEachPresentEntity(cce -> {
					if (!scheduleStack.isEmpty()) {
						for (Entity passenger : cce.getIndirectPassengers()) {
							if (passenger instanceof ConductorEntity conductorEntity && passenger.getUUID().equals(conductorId)) {
								conductorEntity.addSchedule(scheduleStack);
								scheduleStack.setCount(0);
								break;
							}
						}
					}
				});
				if (!scheduleStack.isEmpty() && !player.addItem(scheduleStack))
					player.drop(scheduleStack, false);
			}
		}

		player.sendOverlayMessage(Component.translatable("railways.whistle.success"));
		AllSoundEvents.CONTROLLER_CLICK.play(level, null, pos, 1, 1);

		Schedule schedule = new Schedule();
		ScheduleEntry entry = new ScheduleEntry();
		DestinationInstruction instruction = new DestinationInstruction(AllSchedules.DESTINATION);
		ScheduledDelay condition = new ScheduledDelay(AllSchedules.DELAY);
		condition.getData().putInt("Value", 0);
		instruction.getData().putString("Text", stationName);
		entry.instruction = instruction;
		if (entry.conditions.isEmpty())
			entry.conditions.add(new ArrayList<>());
		entry.conditions.get(0).add(condition);
		schedule.entries.add(entry);
		schedule.cyclic = false;
		train.runtime.discardSchedule();
		for (Carriage carriage : train.carriages)
			carriage.updateConductors();
		train.runtime.setSchedule(schedule, true);
		DiscoveredPath immediatePath = train.runtime.startCurrentInstruction(level);
		if (immediatePath == null && temporaryFlagPos != null && temporaryTrackPos != null) {
			level.setBlock(temporaryFlagPos, Blocks.AIR.defaultBlockState(), 3);
			loadWhistleFlag(level, player, train, temporaryFlagPos, temporaryTrackPos, stationName, selectedColor, !temporaryFlagDirection);
			immediatePath = train.runtime.startCurrentInstruction(level);
		}
		if (immediatePath == null) {
		} else if (immediatePath.destination == train.getCurrentStation()) {
			train.runtime.state = ScheduleRuntime.State.IN_TRANSIT;
			train.runtime.destinationReached();
		} else {
			double navigationResult = train.navigation.startNavigation(immediatePath);
			if (navigationResult != -1) {
				train.status.successfulNavigation();
				train.runtime.state = ScheduleRuntime.State.IN_TRANSIT;
				train.runtime.ticksInTransit = 0;
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull ServerLevel level, @NotNull Entity entity,
							  @NotNull EquipmentSlot slot) {
		boolean held = entity instanceof Player player
			&& (player.getMainHandItem() == stack || player.getOffhandItem() == stack);
		if (held && (level.getGameTime() + entity.hashCode() + slot.ordinal()) % CRConfigs.server().conductors.whistleRebindRate.get() == 0) {
			CompoundTag tag = getStackData(stack);
			Optional<UUID> selectedTrain = getUuid(tag, "SelectedTrain");
			Optional<UUID> selectedConductor = getUuid(tag, "SelectedConductor");
			if (selectedTrain.isPresent() && selectedConductor.isPresent()) {
				UUID trainId = selectedTrain.get();
				UUID conductorId = selectedConductor.get();

				if (level.getEntity(conductorId) instanceof ConductorEntity conductor
					&& conductor.getVehicle() instanceof CarriageContraptionEntity cce && !trainId.equals(cce.trainId)) {
					putUuid(tag, "SelectedTrain", cce.trainId);
					setStackData(stack, tag);
				}
			}
		}
	}

	private static CompoundTag getStackData(ItemStack stack) {
		return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
	}

	private static void setStackData(ItemStack stack, CompoundTag tag) {
		CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
	}

	private static void putUuid(CompoundTag tag, String key, UUID uuid) {
		tag.putString(key, uuid.toString());
	}

	private static boolean carriageHasPassenger(Carriage carriage, UUID passengerId) {
		boolean[] found = {false};
		carriage.forEachPresentEntity(cce -> {
			if (found[0])
				return;
			for (Entity passenger : cce.getIndirectPassengers()) {
				if (passenger.getUUID().equals(passengerId)) {
					found[0] = true;
					return;
				}
			}
		});
		return found[0];
	}

	private static void loadWhistleFlag(Level level, Player player, Train train, BlockPos placePos, BlockPos selectedPos,
										String stationName, byte selectedColor, boolean front) {
		BlockState placeState = CRBlocks.CONDUCTOR_WHISTLE_FLAG.getDefaultState();
		level.setBlock(placePos, placeState, 11);
		CompoundTag beTag = new CompoundTag();
		beTag.putString("Name", stationName);
		beTag.putByte("SelectedColor", selectedColor);
		beTag.putBoolean("TargetDirection", front);
		beTag.store("TargetTrack", BlockPos.CODEC, selectedPos.subtract(placePos));
		TypedEntityData<?> blockEntityData =
			TypedEntityData.of(((IBE<?>) CRBlocks.CONDUCTOR_WHISTLE_FLAG.get()).getBlockEntityType(), beTag);
		BlockEntity blockEntity = level.getBlockEntity(placePos);
		if (blockEntity != null) {
			blockEntityData.loadInto(blockEntity, player.registryAccess());
			blockEntity.setChanged();
			level.sendBlockUpdated(placePos, placeState, placeState, 3);
			if (blockEntity instanceof ConductorWhistleFlagBlockEntity flagBe && flagBe.station != null) {
				flagBe.station.tick();
				if (flagBe.station.getEdgePoint() != null) {
					flagBe.station.getEdgePoint().name = stationName;
				} else {
				}
			}
		} else {
		}
	}

	private static long countStationsNamed(Train train, String stationName) {
		if (train.graph == null)
			return -1;
		return train.graph.getPoints(EdgePointType.STATION).stream()
			.map(GlobalStation.class::cast)
			.filter(station -> Objects.equals(station.name, stationName))
			.count();
	}


	private static Set<Identifier> getValidPathfindingTypes(Train train) {
		Set<Identifier> validTypes = new HashSet<>();
		for (int i = 0; i < train.carriages.size(); i++) {
			Carriage carriage = train.carriages.get(i);
			if (i == 0) {
				validTypes.addAll(carriage.leadingBogey().type.getValidPathfindingTypes(carriage.leadingBogey().getStyle()));
			} else {
				validTypes.retainAll(carriage.leadingBogey().type.getValidPathfindingTypes(carriage.leadingBogey().getStyle()));
			}
			if (carriage.isOnTwoBogeys()) {
				validTypes.retainAll(carriage.trailingBogey().type.getValidPathfindingTypes(carriage.trailingBogey().getStyle()));
			}
		}
		return validTypes;
	}

	private static Optional<UUID> getUuid(CompoundTag tag, String key) {
		if (!tag.contains(key))
			return Optional.empty();
		try {
			return Optional.of(UUID.fromString(tag.getStringOr(key, "")));
		} catch (IllegalArgumentException ignored) {
			return Optional.empty();
		}
	}
}
