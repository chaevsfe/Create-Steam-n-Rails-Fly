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

package com.railwayteam.railways.mixin;

import com.railwayteam.railways.content.conductor.whistle.ConductorWhistleItem;
import com.railwayteam.railways.multiloader.PlayerSelection;
import com.railwayteam.railways.registry.CRPackets;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.deployer.DeployerFakePlayer;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.content.trains.station.StationBlock;
import com.zurrtum.create.content.trains.station.StationBlockEntity;
import com.zurrtum.create.infrastructure.packet.c2s.TrainEditPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StationBlock.class, remap = false)
public abstract class MixinStationBlock {
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
    private void autoWhistle(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir){
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        if (itemInHand.getItem() instanceof ConductorWhistleItem) {
            InteractionResult result = itemInHand.useOn(new UseOnContext(pPlayer, pHand, pHit));
            if (result != InteractionResult.PASS)
                cir.setReturnValue(result);
        }
    }

    @Inject(method = "use", at = @At(value = "RETURN", ordinal = 1), cancellable = true, remap = true)
    private void deployersAssemble(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!pLevel.isClientSide() && pPlayer instanceof DeployerFakePlayer deployerFakePlayer && pLevel.getBlockEntity(pPos) instanceof StationBlockEntity stationBe) {
            cir.setReturnValue(InteractionResult.CONSUME);
            GlobalStation station = stationBe.getStation();
            boolean isAssemblyMode = pState.getValue(StationBlock.ASSEMBLING);
            if (station != null && station.getPresentTrain() == null) {
                //assemble
                if (stationBe.isAssembling() || stationBe.tryEnterAssemblyMode()) {
                    //Need to fix blockstate
                    stationBe.assemble(deployerFakePlayer.getUUID());
                    cir.setReturnValue(InteractionResult.SUCCESS);

                    if (isAssemblyMode) {
                        pLevel.setBlock(pPos, pState.setValue(StationBlock.ASSEMBLING, false), 3);
                        stationBe.refreshBlockState();
                    }
                }
                return;
            }
            BlockState newState = null;
            if (!isAssemblyMode) {
                newState = pState.setValue(StationBlock.ASSEMBLING, true);
            }
            if (disassembleAndEnterMode(deployerFakePlayer, stationBe)) {
                if (newState != null) {
                    pLevel.setBlock(pPos, newState, 3);
                    stationBe.refreshBlockState();

                    stationBe.refreshAssemblyInfo();
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }

    private boolean disassembleAndEnterMode(ServerPlayer sender, StationBlockEntity te) {
        GlobalStation station = te.getStation();
        if (station != null) {
            Train train = station.getPresentTrain();
            BlockPos trackPosition = te.edgePoint.getGlobalPosition();
            ItemStack schedule = train == null ? ItemStack.EMPTY : train.runtime.returnSchedule();
            if (train != null && !train.disassemble(te.getAssemblyDirection(), trackPosition.above()))
                return false;
            dropSchedule(sender, te, schedule);
        }
        return te.tryEnterAssemblyMode();
    }

    private void dropSchedule(ServerPlayer sender, StationBlockEntity te, ItemStack schedule) {
        if (schedule.isEmpty())
            return;
        if (sender.getMainHandItem()
            .isEmpty()) {
            sender.getInventory()
                .placeItemBackInInventory(schedule);
            return;
        }
        Vec3 v = VecHelper.getCenterOf(te.getBlockPos());
        ItemEntity itemEntity = new ItemEntity(te.getLevel(), v.x, v.y, v.z, schedule);
        itemEntity.setDeltaMovement(Vec3.ZERO);
        te.getLevel()
            .addFreshEntity(itemEntity);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
    private void deployersNameTag(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        if (!pLevel.isClientSide() && pPlayer instanceof DeployerFakePlayer
            && pLevel.getBlockEntity(pPos) instanceof StationBlockEntity stationBe
            && itemInHand.getItem() instanceof NameTagItem
        ) {
            cir.setReturnValue(InteractionResult.CONSUME);
            GlobalStation station = stationBe.getStation();
            if (station == null || station.getPresentTrain() == null) return;

            Train train = station.getPresentTrain();
            if (itemInHand.hasCustomHoverName()) { // Set the train name
                String newName = itemInHand.getHoverName().getString();
                if (train.name.getString().equals(newName)) return;

                train.name = Component.literal(newName);
                CRPackets.PACKETS.sendTo(PlayerSelection.all(),
                    new TrainEditPacket.TrainEditReturnPacket(train.id, newName, train.icon.getId(), train.mapColorIndex));
            } else { // Get the train's name and put it on the nametag
                itemInHand.setHoverName(Component.literal(train.name.getString()));
            }
        }
    }
}
