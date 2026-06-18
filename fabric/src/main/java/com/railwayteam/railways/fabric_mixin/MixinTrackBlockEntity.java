package com.railwayteam.railways.fabric_mixin;

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

import java.util.Map;

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

    @Inject(method = "lazyTick", at = @At("HEAD"))
    private void manageCasingCollisions(CallbackInfo ci) {
        if (railways$trackCasing == null || railways$isAlternateModel || level instanceof SchematicLevel)
            return;
        CasingCollisionUtils.manageTracks((TrackBlockEntity) (Object) this, false);
    }
}
