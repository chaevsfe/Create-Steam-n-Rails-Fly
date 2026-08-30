package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.custom_tracks.casing.CasingChecker;
import com.railwayteam.railways.content.custom_tracks.casing.CasingCollisionUtils;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.content.trains.track.BezierConnection;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlockEntity;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.storage.ValueInput;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@Mixin(value = TrackBlockEntity.class, remap = false)
public abstract class MixinTrackBlockEntity extends SmartBlockEntity implements IHasTrackCasing {
    @Shadow
    Map<BlockPos, BezierConnection> connections;

    @Unique
    protected Block railways$trackCasing;
    @Unique
    protected boolean railways$isAlternateModel;

    protected MixinTrackBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable Block railways$getTrackCasing() {
        return railways$trackCasing;
    }

    public void railways$setTrackCasing(@Nullable Block trackCasing) {
        if (trackCasing != null && !CasingChecker.isValid(trackCasing))
            return;
        this.railways$trackCasing = trackCasing;
        notifyUpdate();
        if (this.level == null)
            return;

        if (this.railways$trackCasing == null) {
            CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, true);
            if (!this.level.isClientSide()) {
                if (!this.connections.isEmpty() || getBlockState().getOptionalValue(TrackBlock.SHAPE)
                    .orElse(TrackShape.NONE)
                    .isPortal())
                    return;
                BlockState blockState = this.level.getBlockState(worldPosition);
                if (blockState.hasProperty(TrackBlock.HAS_BE))
                    level.setBlockAndUpdate(worldPosition, blockState.setValue(TrackBlock.HAS_BE, false));
            }
        } else if (!railways$isAlternateModel) {
            CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, false);
        }
    }

    public boolean railways$isAlternate() {
        return railways$isAlternateModel;
    }

    public void railways$setAlternate(boolean alternate) {
        if (getBlockState().getValue(TrackBlock.SHAPE).getModel().equals("ascending"))
            alternate = false;
        this.railways$isAlternateModel = alternate;
        if (railways$trackCasing != null)
            CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, alternate);
        notifyUpdate();
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void writeCasing(ValueOutput tag, boolean clientPacket, CallbackInfo ci) {
        Block casing = railways$getTrackCasing();
        if (casing != null)
            tag.putString("TrackCasing", BuiltInRegistries.BLOCK.getKey(casing).toString());
        tag.putBoolean("AlternateModel", this.railways$isAlternate());
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void readCasing(ValueInput tag, boolean clientPacket, CallbackInfo ci) {
        railways$setAlternate(tag.getBooleanOr("AlternateModel", false));

        if (tag.getString("TrackCasing").isPresent()) {
            Identifier casingName = Identifier.parse(tag.getStringOr("TrackCasing", ""));
            if (BuiltInRegistries.BLOCK.containsKey(casingName)) {
                BuiltInRegistries.BLOCK.get(casingName)
                    .ifPresent(holder -> railways$setTrackCasing(holder.value()));
                return;
            }
        }
        railways$setTrackCasing(null);
    }

    @Inject(
        method = "removeConnection",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
        ),
        cancellable = true
    )
    private void railways$preventCasedTileRemoval(BlockPos target, CallbackInfo ci) {
        if (railways$getTrackCasing() != null) {
            notifyUpdate();
            ci.cancel();
        }
    }

    @Inject(
        method = "removeInboundConnections",
        at = @At(
            value = "INVOKE",
            target = "Lcom/zurrtum/create/infrastructure/packet/s2c/RemoveBlockEntityPacket;<init>(Lnet/minecraft/core/BlockPos;)V"
        ),
        cancellable = true
    )
    private void railways$preventCasedTileRemovalInbound(boolean dropItems, CallbackInfo ci) {
        if (railways$getTrackCasing() != null) {
            notifyUpdate();
            ci.cancel();
        }
    }

    @Inject(method = "preRemoveSideEffects", at = @At("HEAD"))
    private void railways$removeCasingCollisions(BlockPos pos, BlockState oldState, CallbackInfo ci) {
        if (level == null || level.isClientSide() || level instanceof SchematicLevel)
            return;
        CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, true);
    }

    @Inject(method = "lazyTick", at = @At("HEAD"))
    private void manageCasingCollisions(CallbackInfo ci) {
        if (railways$trackCasing == null || railways$isAlternateModel || level instanceof SchematicLevel)
            return;
        CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, false);
    }

    /**
     * Old ponder schematics (pre-Create-Fly) saved BezierConnection data in a different NBT format.
     * Create Fly's BezierConnection constructor calls orElseThrow() on required codec fields, crashing
     * when those fields are absent. Wrap the forEach so each entry is skipped (with a warning) rather
     * than crashing the whole ponder scene load.
     */
    @SuppressWarnings("unchecked")
    @WrapOperation(method = "read",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ValueInput$ValueInputList;forEach(Ljava/util/function/Consumer;)V"),
        remap = false)
    private void railways$safeLoadBezierConnections(ValueInput.ValueInputList list, Consumer<?> consumer, Operation<Void> original) {
        for (ValueInput item : list) {
            try {
                ((Consumer<Object>) consumer).accept(item);
            } catch (NoSuchElementException e) {
                Railways.LOGGER.warn("railways: Skipping BezierConnection with incompatible format (old schematic?): {}", e.getMessage());
            }
        }
    }
}
