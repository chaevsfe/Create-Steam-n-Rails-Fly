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

import com.railwayteam.railways.Railways;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ConductorEntityModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Railways.asResource("conductor"), "main");

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		root.addOrReplaceChild("hat",
			CubeListBuilder.create().texOffs(0, 48)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)),
			PartPose.offsetAndRotation(0.0F, 10.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		root.addOrReplaceChild("real_hat",
			CubeListBuilder.create().texOffs(32, 25)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.2F)),
			PartPose.offset(0.0F, 10.0F, 0.0F));

		root.addOrReplaceChild("head",
			CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 10.0F, 0.0F));

		root.addOrReplaceChild("left_arm",
			CubeListBuilder.create().texOffs(50, 0)
				.addBox(0.0F, -2.0F, -2.0F, 3.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offset(4.0F, 12.0F, 0.0F));

		root.addOrReplaceChild("right_arm",
			CubeListBuilder.create().texOffs(34, 0)
				.addBox(-3.0F, -2.0F, -2.0F, 3.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-4.0F, 12.0F, 0.0F));

		root.addOrReplaceChild("body",
			CubeListBuilder.create().texOffs(0, 17)
				.addBox(-4.0F, -9.0F, -3.0F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 29)
				.addBox(-3.0F, -4.0F, -2.0F, 6.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offset(0.0F, 19.0F, 0.0F));

		root.addOrReplaceChild("right_leg",
			CubeListBuilder.create().texOffs(34, 15)
				.addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offset(1.5F, 19.0F, 0.0F));

		root.addOrReplaceChild("left_leg",
			CubeListBuilder.create().texOffs(50, 15)
				.addBox(-1.5F, 0.0F, -2.0F, 3.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
			PartPose.offset(-1.5F, 19.0F, 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}
}
