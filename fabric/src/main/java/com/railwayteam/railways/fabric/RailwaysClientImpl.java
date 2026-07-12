/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
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

package com.railwayteam.railways.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.RailwaysClient;
import com.railwayteam.railways.base.reload.ClientResourceReloadListener;
import com.railwayteam.railways.content.buffer.headstock.fabric.CopycatHeadstockModelRegistration;
import com.railwayteam.railways.content.conductor.fabric.ConductorCapItemRenderer;
import com.railwayteam.railways.content.custom_tracks.monorail.MonorailClientRuntimeChecks;
import com.railwayteam.railways.events.ClientEvents;
import com.railwayteam.railways.registry.CRParticleTypes;
import com.railwayteam.railways.registry.fabric.CRBlockEntitiesClientImpl;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RailwaysClientImpl implements ClientModInitializer {
	public void onInitializeClient() {
		CopycatHeadstockModelRegistration.register();
		CRBlockEntitiesClientImpl.register();
		RailwaysClient.init();
		MonorailClientRuntimeChecks.run();
		if (FabricLoader.getInstance().isModLoaded("jei")) {
			registerJeiCompat();
		}
		ConductorCapItemRenderer.register();
		CRParticleTypes.registerFactories();
		registerClientEvents();
	}

	private static void registerJeiCompat() {
		try {
			Class.forName("com.railwayteam.railways.compat.jei.RailwaysJeiClient")
				.getMethod("register")
				.invoke(null);
		} catch (ReflectiveOperationException e) {
			Railways.LOGGER.error("Failed to register Railways JEI client compatibility", e);
		}
	}

	private static void registerClientEvents() {
		ClientTickEvents.START_CLIENT_TICK.register(ClientEvents::onClientTickStart);
		ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTickEnd);
		ClientLevelEvents.AFTER_CLIENT_LEVEL_CHANGE.register((mc, level) -> ClientEvents.onClientWorldLoad(level));
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Railways.asResource("client_events");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				ClientEvents.onTagsUpdated();
			}
		});
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			@Override
			public Identifier getFabricId() {
				return Railways.asResource("client_resource_reload");
			}

			@Override
			public void onResourceManagerReload(ResourceManager resourceManager) {
				ClientResourceReloadListener.INSTANCE.onResourceManagerReload(resourceManager);
			}
		});
	}

	@SuppressWarnings({"unchecked", "rawtypes"}) // jank!
	public static void registerClientCommands(Consumer<CommandDispatcher<SharedSuggestionProvider>> consumer) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CommandDispatcher<SharedSuggestionProvider> casted = (CommandDispatcher) dispatcher;
			consumer.accept(casted);
		});
	}

	public static void registerModelLayer(ModelLayerLocation layer, Supplier<LayerDefinition> definition) {
		ModelLayerRegistry.registerModelLayer(layer, definition::get);
	}

	public static <T extends Entity> void registerEntityRenderer(EntityType<? extends T> type, EntityRendererProvider<T> provider) {
		EntityRenderers.register(type, provider);
	}

	public static void registerBuiltinPack(String id, String name) {
		ModContainer mod = FabricLoader.getInstance().getModContainer(Railways.MOD_ID).orElseThrow();
		ResourceManagerHelper.registerBuiltinResourcePack(Railways.asResource(id), mod, Component.literal(name), ResourcePackActivationType.NORMAL);
	}
}
