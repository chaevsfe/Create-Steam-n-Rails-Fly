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

package com.railwayteam.railways.fabric_mixin;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.custom_tracks.casing.CasingChecker;
import com.railwayteam.railways.mixin_interfaces.IHasTrackCasing;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.trains.track.BezierConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BezierConnection.class, remap = false)
public abstract class MixinBezierConnection implements IHasTrackCasing {
    @Shadow public Couple<BlockPos> bePositions;

    @Shadow public abstract Vec3 getPosition(double t);

    @Unique
    protected Block railways$trackCasing;
    @Unique
    protected boolean railways$isShiftedDown;

    public @Nullable Block railways$getTrackCasing() {
        return railways$trackCasing;
    }

    public void railways$setTrackCasing(@Nullable Block trackCasing) {
        if (trackCasing != null && !CasingChecker.isValid(trackCasing))
            return;
        this.railways$trackCasing = trackCasing;
    }

    public boolean railways$isAlternate() {
        return railways$isShiftedDown;
    }

    public void railways$setAlternate(boolean alternate) {
        this.railways$isShiftedDown = alternate;
    }

    @Inject(method = "write(Lnet/minecraft/world/level/storage/ValueOutput;Lnet/minecraft/core/BlockPos;)V", at = @At("RETURN"), remap = false)
    private void write(ValueOutput output, BlockPos localTo, CallbackInfo ci) {
        Block casing = railways$getTrackCasing();
        if (casing != null) {
            if (BuiltInRegistries.BLOCK.getKey(casing).toString().equals("minecraft:block")) {
                Railways.LOGGER.error("NBTwrite trackCasing was minecraft:block!!! for BezierConnection: primary={}, secondary={}, casing={}",
                    bePositions.getFirst(), bePositions.getSecond(), casing);
            } else {
                output.putString("Casing", BuiltInRegistries.BLOCK.getKey(casing).toString());
            }
        }
        output.putBoolean("ShiftDown", railways$isAlternate());
    }

    @Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"), remap = false)
    private void netWrite(FriendlyByteBuf buffer, CallbackInfo ci) {
        Block casing = railways$getTrackCasing();
        buffer.writeBoolean(casing != null);
        if (casing != null) {
            buffer.writeIdentifier(BuiltInRegistries.BLOCK.getKey(casing));
            buffer.writeBoolean(railways$isAlternate());
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/world/level/storage/ValueInput;Lnet/minecraft/core/BlockPos;)V", at = @At("RETURN"), remap = false)
    private void nbtConstructor(ValueInput input, BlockPos localTo, CallbackInfo ci) {
        input.getString("Casing").ifPresent(casingId -> {
            if (casingId.equals("minecraft:block"))
                Railways.LOGGER.error("NBTCtor trackCasing was minecraft:block!!! for BezierConnection: primary={}, secondary={}", bePositions.getFirst(), bePositions.getSecond());
            BuiltInRegistries.BLOCK.get(Identifier.parse(casingId))
                .ifPresent(holder -> railways$setTrackCasing(holder.value()));
        });
        railways$setAlternate(input.getBooleanOr("ShiftDown", false));
    }

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At("RETURN"), remap = false)
    private void byteBufConstructor(FriendlyByteBuf buffer, CallbackInfo ci) {
        if (buffer.readBoolean()) {
            BuiltInRegistries.BLOCK.get(buffer.readIdentifier())
                .ifPresent(holder -> railways$setTrackCasing(holder.value()));
            railways$setAlternate(buffer.readBoolean());
        } else {
            railways$setTrackCasing(null);
        }
    }

    @Inject(method = "spawnItems", at = @At("TAIL"))
    private void spawnCasing(Level level, CallbackInfo ci) {
        Block casing = railways$getTrackCasing();
        if (casing != null) {
            Vec3 spawnPos = getPosition(0.5);
            ItemEntity entity = new ItemEntity(level, spawnPos.x, spawnPos.y, spawnPos.z, new ItemStack(casing));
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
    }

    @Inject(method = "addItemsToPlayer", at = @At("TAIL"))
    private void addCasingItem(Player player, CallbackInfo ci) {
        Block casing = railways$getTrackCasing();
        if (casing != null) {
            Inventory inv = player.getInventory();
            inv.placeItemBackInInventory(new ItemStack(casing));
        }
    }
}
