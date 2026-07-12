package com.railwayteam.railways.fabric_mixin.conductor_possession;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.conductor.ClientHandler;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void setPostEffect(Identifier postEffectId);

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void railways$bobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (!(minecraft.getCameraEntity() instanceof ConductorEntity))
            return;

        float walk = cameraState.entityRenderState.backwardsInterpolatedWalkDistance;
        float bob = cameraState.entityRenderState.bob;
        poseStack.translate(Mth.sin(walk * (float) Math.PI) * bob * 0.5f,
            -Math.abs(Mth.cos(walk * (float) Math.PI) * bob), 0.0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(walk * (float) Math.PI) * bob * 3.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(walk * (float) Math.PI - 0.2f) * bob) * 5.0f));
        ci.cancel();
    }

    @Inject(method = "checkEntityPostEffect", at = @At("RETURN"))
    private void railways$checkEntityPostEffect(Entity entity, CallbackInfo ci) {
        if (entity instanceof ConductorEntity && CRConfigs.client().useConductorSpyShader.get())
            setPostEffect(Identifier.fromNamespaceAndPath("railways", "scan_pincushion"));
    }

    @Inject(method = "shouldRenderBlockOutline", at = @At("HEAD"), cancellable = true)
    private void railways$shouldRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (!ClientHandler.isPlayerMountedOnCamera())
            return;

        boolean shouldRender = !minecraft.gui.hud.isHidden();
        HitResult hitResult = minecraft.hitResult;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && minecraft.level != null
            && hitResult instanceof BlockHitResult blockHitResult) {
            shouldRender &= ConductorEntity.canSpyInteract(minecraft.level.getBlockState(blockHitResult.getBlockPos()));
        } else {
            shouldRender = false;
        }
        cir.setReturnValue(shouldRender);
    }
}
