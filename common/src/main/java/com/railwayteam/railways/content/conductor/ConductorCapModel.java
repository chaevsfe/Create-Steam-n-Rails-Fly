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
		return new ConductorCapModel<>(base.head);
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
