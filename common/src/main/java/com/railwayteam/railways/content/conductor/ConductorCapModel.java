/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.conductor;

import com.railwayteam.railways.Railways;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class ConductorCapModel<T extends LivingEntity> extends Model implements HeadedModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Railways.asResource("conductor_cap"), "main");

	private final ModelPart cap;

	public ConductorCapModel(ModelPart root) {
		super(root, ignored -> null);
		this.cap = root.hasChild("cap") ? root.getChild("cap") : root;
	}

	public ConductorCapModel(ModelPart root, Object ignoredOverride, boolean ignoredTilt) {
		this(root);
	}

	public static void clearModelCache() {
	}

	public static ConductorCapModel<?> of(ItemStack stack, HumanoidModel<?> base, LivingEntity entity) {
		ConductorCapModel<?> model = new ConductorCapModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(LAYER_LOCATION));
		model.setProperties(base);
		return model;
	}

	public void setProperties(HumanoidModel<?> base) {
		copyPart(base.hat, cap);
		float s = 1 / 16f;
		cap.offsetScale(new Vector3f(s, s, s));
		cap.offsetPos(new Vector3f(0, 0, -10 / 16f));
	}

	private static void copyPart(ModelPart source, ModelPart target) {
		target.x = source.x;
		target.y = source.y;
		target.z = source.z;
		target.xRot = source.xRot;
		target.yRot = source.yRot;
		target.zRot = source.zRot;
		target.xScale = source.xScale;
		target.yScale = source.yScale;
		target.zScale = source.zScale;
		target.visible = source.visible;
		target.skipDraw = source.skipDraw;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition cap = root.addOrReplaceChild("cap",
			CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.5F, -9.0F, -4.0F, 9.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)),
			PartPose.ZERO);

		cap.addOrReplaceChild("brim",
			CubeListBuilder.create().texOffs(6, 12)
				.addBox(-4.5F, 3.8F, -3.0F, 9.0F, 0.02F, 3.0F, new CubeDeformation(0.0F)),
			PartPose.offsetAndRotation(0.0F, -10.0F, -4.0F, 0.2618F, 0.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	public ModelPart getHead() {
		return cap;
	}
}
