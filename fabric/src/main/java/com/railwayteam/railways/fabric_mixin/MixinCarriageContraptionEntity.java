/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.zurrtum.create.AllItems;
import net.minecraft.world.InteractionHand;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.switches.TrackSwitch;
import com.railwayteam.railways.content.switches.TrackSwitchBlock.SwitchState;
import com.railwayteam.railways.mixin_interfaces.IDistanceTravelled;
import com.railwayteam.railways.mixin_interfaces.IGenerallySearchableNavigation;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.util.MixinVariables;
import com.railwayteam.railways.util.packet.SwitchDataUpdatePacket;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Navigation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;

@Mixin(value = CarriageContraptionEntity.class, remap = false)
public abstract class MixinCarriageContraptionEntity extends OrientedContraptionEntity implements IDistanceTravelled {
    @Shadow private Carriage carriage;

    private MixinCarriageContraptionEntity(EntityType<? extends OrientedContraptionEntity> type, Level world) {
        super(type, world);
    }

    @Unique private boolean railways$switchMessage = false;
    @Unique private double railways$distanceTravelled;

    @WrapOperation(
        method = "tickContraption",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/content/trains/entity/CarriageBogey;updateAngles(Lcom/zurrtum/create/content/trains/entity/CarriageContraptionEntity;D)V",
            ordinal = 0
        )
    )
    private void railways$storeDistanceTravelled(
        CarriageBogey bogey,
        CarriageContraptionEntity entity,
        double distanceMoved,
        Operation<Void> original
    ) {
        original.call(bogey, entity, distanceMoved);
        railways$distanceTravelled = distanceMoved;
    }

    @Override
    public double railways$getDistanceTravelled() {
        return railways$distanceTravelled;
    }

    @Inject(
        method = "updateTrackGraph",
        at = @At(
            value = "FIELD",
            target = "Lcom/zurrtum/create/content/trains/entity/Train;graph:Lcom/zurrtum/create/content/trains/graph/TrackGraph;",
            opcode = Opcodes.PUTFIELD,
            ordinal = 0
        ),
        cancellable = true
    )
    private void railways$cancelClientDerailing(CallbackInfo ci) {
        if (level().isClientSide() && CRConfigs.client().skipClientDerailing.get())
            ci.cancel();
    }

    @Inject(method = "control", at = @At("TAIL"))
    private void railways$handcarHungerDepletion(BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player,
                                                 CallbackInfoReturnable<Boolean> cir) {
        if (((IHandcarTrain) this.carriage.train).railways$isHandcar()
            && !player.getItemInHand(InteractionHand.MAIN_HAND).is(AllItems.EXTENDO_GRIP))
            player.causeFoodExhaustion((float) Math.abs(carriage.train.speed) * CRConfigs.server().handcarHungerMultiplier.getF());
    }

    @Inject(method = "control", at = @At("TAIL"))
    private void railways$showSwitchOverlay(BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player,
                                            CallbackInfoReturnable<Boolean> cir) {
        Navigation nav = carriage.train.navigation;

        StructureBlockInfo info = contraption.getBlocks().get(controlsLocalPos);
        Direction initialOrientation = getInitialOrientation().getCounterClockWise();
        boolean inverted = false;
        if (info != null && info.state().hasProperty(ControlsBlock.FACING))
            inverted = !info.state().getValue(ControlsBlock.FACING).equals(initialOrientation);

        int targetSpeed = 0;
        if (heldControls.contains(0)) targetSpeed++;
        if (heldControls.contains(1)) targetSpeed--;
        if (inverted) targetSpeed *= -1;

        boolean spaceDown = heldControls.contains(4);
        double directedSpeed = targetSpeed != 0 ? targetSpeed : carriage.train.speed;
        boolean forward = !carriage.train.doubleEnded || (directedSpeed != 0 ? directedSpeed > 0 : !inverted);

        boolean previousSkipSwitches = MixinVariables.temporarilySkipSwitches;
        Pair<TrackSwitch, Pair<Boolean, Optional<SwitchState>>> lookAheadData;
        MixinVariables.temporarilySkipSwitches = true;
        try {
            lookAheadData = ((IGenerallySearchableNavigation) nav).railways$findNearestApproachableSwitch(forward);
        } finally {
            MixinVariables.temporarilySkipSwitches = previousSkipSwitches;
        }

        TrackSwitch lookAhead = lookAheadData == null ? null : lookAheadData.getFirst();
        boolean headOn = lookAheadData != null && lookAheadData.getSecond().getFirst();
        Optional<SwitchState> targetState = lookAheadData == null ? Optional.empty() : lookAheadData.getSecond().getSecond();

        if (lookAhead != null) {
            if (CRConfigs.server().flipDistantSwitches.get() && spaceDown && lookAhead.isAutomatic()
                    && !lookAhead.isLocked() && !carriage.train.navigation.isActive()) {
                if (headOn)
                    lookAhead.trySetSwitchState(SwitchState.fromSteerDirection(carriage.train.manualSteer, forward));
                else
                    targetState.ifPresent(lookAhead::trySetSwitchState);
            }
            boolean wrong = headOn
                    ? SwitchState.fromSteerDirection(carriage.train.manualSteer, forward) != lookAhead.getSwitchState()
                    : targetState.isPresent() && lookAhead.getSwitchState() != targetState.get();
            if (player instanceof ServerPlayer sp)
                CRPackets.PACKETS.sendTo(sp, new SwitchDataUpdatePacket(lookAhead.getSwitchState(), lookAhead.isAutomatic(), wrong, lookAhead.isLocked()));
            railways$switchMessage = true;
        } else {
            if (railways$switchMessage) {
                if (player instanceof ServerPlayer sp)
                    CRPackets.PACKETS.sendTo(sp, SwitchDataUpdatePacket.clear());
                railways$switchMessage = false;
            }
        }
    }
}
