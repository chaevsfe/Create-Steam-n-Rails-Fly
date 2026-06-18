/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.conductor;

import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.util.packet.CameraMovePacket;
import com.railwayteam.railways.util.packet.DismountCameraPacket;
import com.railwayteam.railways.util.packet.SpyConductorInteractPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ConductorPossessionController {
	@Environment(EnvType.CLIENT)
	private static ClientChunkCache.Storage cameraStorage;
	@Environment(EnvType.CLIENT)
	public static CameraType previousCameraType;
	private static boolean wasUpPressed;
	private static boolean wasDownPressed;
	private static boolean wasLeftPressed;
	private static boolean wasRightPressed;
	private static boolean wasJumpPressed;
	private static boolean wasSprintPressed;
	private static boolean wasMounted;
	private static final boolean[] wasMousePressed = new boolean[3];
	private static boolean wasUsingBefore;
	private static int positionReminder;

	@Environment(EnvType.CLIENT)
	public static void onClientTick(Minecraft mc, boolean start) {
		Entity cameraEntity = mc.getCameraEntity();
		if (cameraEntity instanceof ConductorEntity) {
			wasMounted = true;
			Options options = mc.options;
			if (start) {
				if (wasUpPressed = options.keyUp.isDown())
					options.keyUp.setDown(false);
				if (wasDownPressed = options.keyDown.isDown())
					options.keyDown.setDown(false);
				if (wasLeftPressed = options.keyLeft.isDown())
					options.keyLeft.setDown(false);
				if (wasRightPressed = options.keyRight.isDown())
					options.keyRight.setDown(false);
				if (wasJumpPressed = options.keyJump.isDown())
					options.keyJump.setDown(false);
				wasSprintPressed = options.keySprint.isDown();
				if (options.keyShift.isDown()) {
					dismount();
					options.keyShift.setDown(false);
				}
			} else {
				if (wasUpPressed)
					options.keyUp.setDown(true);
				if (wasDownPressed)
					options.keyDown.setDown(true);
				if (wasLeftPressed)
					options.keyLeft.setDown(true);
				if (wasRightPressed)
					options.keyRight.setDown(true);
				if (wasJumpPressed)
					options.keyJump.setDown(true);

				ConductorEntity conductor = (ConductorEntity) cameraEntity;
				double dx = conductor.getX() - conductor.xLast;
				double dy = conductor.getY() - conductor.yLast;
				double dz = conductor.getZ() - conductor.zLast;
				double dyRot = conductor.getYRot() - conductor.yRotLast;
				double dxRot = conductor.getXRot() - conductor.xRotLast;
				++positionReminder;
				boolean moved = Mth.lengthSquared(dx, dy, dz) > Mth.square(2.0E-4) || positionReminder >= 20;
				boolean rotated = dyRot != 0.0 || dxRot != 0.0;
				if (moved || rotated) {
					positionReminder = 0;
					conductor.xLast = conductor.getX();
					conductor.yLast = conductor.getY();
					conductor.zLast = conductor.getZ();
					conductor.yRotLast = conductor.getYRot();
					conductor.xRotLast = conductor.getXRot();
					CRPackets.PACKETS.send(new CameraMovePacket(conductor, conductor.getX(), conductor.getY(), conductor.getZ(),
						conductor.getYRot(), conductor.getXRot(), conductor.onGround()));
				}
			}
		} else if (wasMounted) {
			wasMounted = false;
			positionReminder = 0;
			dismount();
			if (mc.levelRenderer != null)
				mc.levelRenderer.allChanged();
		}
	}

	@Environment(EnvType.CLIENT)
	public static void onHandleKeybinds(Minecraft mc, boolean start) {
		if (!(mc.getCameraEntity() instanceof ConductorEntity))
			return;

		wasMounted = true;
		Options options = mc.options;
		if (start) {
			Arrays.fill(wasMousePressed, false);
			if (wasMousePressed[0] = options.keyAttack.isDown())
				options.keyAttack.setDown(false);
			if (wasMousePressed[1] = options.keyUse.isDown())
				options.keyUse.setDown(false);
			if (wasMousePressed[2] = options.keyPickItem.isDown())
				options.keyPickItem.setDown(false);
		} else {
			if (wasMousePressed[0])
				options.keyAttack.setDown(true);
			if (wasMousePressed[1]) {
				options.keyUse.setDown(true);
				if (!wasUsingBefore) {
					wasUsingBefore = true;
					HitResult hitResult = mc.hitResult;
					if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && mc.level != null
						&& hitResult instanceof BlockHitResult blockHitResult
						&& ConductorEntity.canSpyInteract(mc.level.getBlockState(blockHitResult.getBlockPos()))) {
						CRPackets.PACKETS.send(new SpyConductorInteractPacket(blockHitResult.getBlockPos()));
					}
				}
			} else {
				wasUsingBefore = false;
			}
			if (wasMousePressed[2])
				options.keyPickItem.setDown(true);
		}
	}

	@Environment(EnvType.CLIENT)
	public static void dismount() {
		CRPackets.PACKETS.send(new DismountCameraPacket());
		wasMounted = false;
	}

	@Environment(EnvType.CLIENT)
	public static ClientChunkCache.Storage getCameraStorage() {
		return cameraStorage;
	}

	@Environment(EnvType.CLIENT)
	public static void setCameraStorage(ClientChunkCache.Storage newStorage) {
		cameraStorage = newStorage;
	}

	@Environment(EnvType.CLIENT)
	public static void setRenderPosition(Entity entity) {
		if (entity instanceof ConductorEntity && cameraStorage != null) {
			cameraStorage.viewCenterX = entity.chunkPosition().x;
			cameraStorage.viewCenterZ = entity.chunkPosition().z;
		}
	}

	@Environment(EnvType.CLIENT)
	public static void tryUpdatePossession(ConductorEntity conductorEntity) {
		if (ClientHandler.getPlayerMountedOnCamera() == conductorEntity)
			setRenderPosition(conductorEntity);
	}

	public static boolean isPossessingConductor(Entity entity) {
		if (!(entity instanceof Player player))
			return false;

		if (player.level().isClientSide())
			return ClientHandler.isPlayerMountedOnCamera();
		return player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
			&& serverPlayer.getCamera() instanceof ConductorEntity;
	}

	@Nullable
	public static ConductorEntity getPossessingConductor(Entity entity) {
		if (!(entity instanceof Player player))
			return null;

		if (player.level().isClientSide())
			return ClientHandler.getPlayerMountedOnCamera();
		if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
			&& serverPlayer.getCamera() instanceof ConductorEntity conductor)
			return conductor;
		return null;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasUpPressed() {
		return wasUpPressed;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasDownPressed() {
		return wasDownPressed;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasLeftPressed() {
		return wasLeftPressed;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasRightPressed() {
		return wasRightPressed;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasSprintPressed() {
		return wasSprintPressed;
	}

	@Environment(EnvType.CLIENT)
	public static boolean wasJumpPressed() {
		return wasJumpPressed;
	}
}
