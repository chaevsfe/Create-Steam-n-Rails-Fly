package com.railwayteam.railways.content.conductor;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class ConductorRenderModel extends EntityModel<ConductorRenderState> implements HeadedModel {
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart realHat;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ConductorRenderModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.hat = root.getChild("hat");
        this.realHat = root.getChild("real_hat");
        this.body = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        // The outer hat overlay (hat) is not rendered in the main pass —
        // it was explicitly excluded in the original renderToBuffer. Since
        // renderToBuffer is final in 1.21.11, we hide it here instead.
        this.hat.visible = false;
    }

    @Override
    public void setupAnim(ConductorRenderState state) {
        super.setupAnim(state);

        float limbSwing = state.walkAnimationPos;
        float limbSwingAmount = state.walkAnimationSpeed;

        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
        realHat.xRot = head.xRot;
        realHat.yRot = head.yRot;

        hat.xRot = head.xRot + (-10 * Mth.DEG_TO_RAD);
        hat.yRot = head.yRot;
        float amt = -1.6f;
        hat.x = (float) (Math.cos(head.xRot) * amt * Math.sin(head.yRot));
        hat.z = (float) (Math.cos(head.xRot) * amt * Math.cos(head.yRot));
        hat.y = 10.0f - (float) (Math.sin(head.xRot) * amt);

        rightArm.xRot = Mth.cos(limbSwing * 0.6662F * 2 + Mth.PI) * 2.0F * limbSwingAmount * 0.5F;
        leftArm.xRot = Mth.cos(limbSwing * 0.6662F * 2) * 2.0F * limbSwingAmount * 0.5F;
        rightArm.zRot = 0.0f;
        leftArm.zRot = 0.0f;
        rightArm.yRot = 0.0f;
        leftArm.yRot = 0.0f;

        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F * 3) * 1.4F * limbSwingAmount;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F * 3 + Mth.PI) * 1.4F * limbSwingAmount;
        rightLeg.yRot = 0.0F;
        leftLeg.yRot = 0.0F;
        rightLeg.zRot = 0.0F;
        leftLeg.zRot = 0.0F;

        body.xRot = 0.0f;
        head.y = 10.0f;
        body.y = 19.0f;
        rightArm.y = 12.0f;
        leftArm.y = 12.0f;
        rightLeg.y = 19.0f;
        leftLeg.y = 19.0f;
    }

    @Override
    public ModelPart getHead() {
        return head;
    }
}
