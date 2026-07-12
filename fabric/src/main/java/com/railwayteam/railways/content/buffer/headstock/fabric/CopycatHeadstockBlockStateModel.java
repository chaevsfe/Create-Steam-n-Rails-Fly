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

package com.railwayteam.railways.content.buffer.headstock.fabric;

import com.google.common.base.Suppliers;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.railwayteam.railways.content.buffer.IDyedBuffer;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBarsBlock;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.railwayteam.railways.registry.CRBlocks;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatSpecialCases;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.railwayteam.railways.content.buffer.BufferModelUtils.getSwapper;

public class CopycatHeadstockBlockStateModel extends CopycatModel {
	private static final AABB CUBE_AABB = new AABB(BlockPos.ZERO);
	private static final Identifier COPYCAT_BASE_TEXTURE = Identifier.fromNamespaceAndPath("create", "block/copycat_base");

	public CopycatHeadstockBlockStateModel(BlockState state, BlockStateModel.UnbakedRoot unbaked) {
		super(state, unbaked);
	}

	public CopycatHeadstockBlockStateModel(BlockState state, BlockStateModel.Unbaked unbaked) {
		this(state, unbaked.asRoot());
	}

	@Override
	protected void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, CopycatBlock block,
									BlockState material, RandomSource random, List<BlockStateModelPart> parts) {
		UnaryOperator<TextureAtlasSprite> colorSwapper = world.getBlockEntity(pos) instanceof IDyedBuffer dyed
			? getSwapper(dyed.getColor())
			: null;
		addWrappedParts(random, colorSwapper, parts);
		DirectionData directionData = gatherDirectionData(block, state);
		if (addSpecialBarsParts(world, pos, state, material, random, directionData, parts))
			return;
		addHeadstockParts(directionData, state, getMaterialParts(world, pos, material, random, getModelOf(material)), parts);
	}

	private void addWrappedParts(RandomSource random, UnaryOperator<TextureAtlasSprite> colorSwapper,
								 List<BlockStateModelPart> parts) {
		List<BlockStateModelPart> wrappedParts = new ArrayList<>();
		model.collectParts(random, wrappedParts);
		for (BlockStateModelPart part : wrappedParts) {
			QuadCollection.Builder builder = new QuadCollection.Builder();
			addFilteredWrappedQuads(part.getQuads(null), colorSwapper, builder::addUnculledFace);
			for (Direction direction : Iterate.directions)
				addFilteredWrappedQuads(
					part.getQuads(direction),
					colorSwapper,
					quad -> builder.addCulledFace(direction, quad)
				);
			QuadCollection quads = builder.build();
			if (!quads.getAll().isEmpty())
				parts.add(new SimpleModelWrapper(quads, part.useAmbientOcclusion(), part.particleMaterial()));
		}
	}

	private void addFilteredWrappedQuads(List<BakedQuad> source, UnaryOperator<TextureAtlasSprite> colorSwapper,
									 Consumer<BakedQuad> consumer) {
		List<BakedQuad> quads = colorSwapper == null ? source : BakedModelHelper.swapSprites(source, colorSwapper);
		for (BakedQuad quad : quads) {
			if (!isCopycatBase(quad))
				consumer.accept(quad);
		}
	}

	static boolean isCopycatBase(BakedQuad quad) {
		return COPYCAT_BASE_TEXTURE.equals(quad.materialInfo().sprite().contents().name());
	}

	private boolean addSpecialBarsParts(BlockAndTintGetter world, BlockPos pos, BlockState state,
									BlockState material, RandomSource random, DirectionData directionData,
									List<BlockStateModelPart> parts) {
		if (!CopycatSpecialCases.isBarsMaterial(material))
			return false;

		Direction facing = state.getOptionalValue(CopycatHeadstockBlock.FACING).orElse(Direction.NORTH);
		boolean upsideDown = state.getOptionalValue(CopycatHeadstockBlock.UPSIDE_DOWN).orElse(false);
		BlockState specialState = CRBlocks.COPYCAT_HEADSTOCK_BARS.getDefaultState()
			.setValue(CopycatHeadstockBarsBlock.FACING, facing)
			.setValue(CopycatHeadstockBarsBlock.UPSIDE_DOWN, upsideDown);
		BlockStateModel specialModel = getModelOf(specialState);
		if (!(specialModel instanceof CopycatHeadstockBarsModel barsModel))
			return false;

		barsModel.addMaterialParts(world, pos, material, random, directionData::isUncull, parts);
		return true;
	}

	static List<BakedQuad> getItemMaterialQuads(BlockState material, RandomSource random) {
		if (CopycatSpecialCases.isBarsMaterial(material)) {
			BlockState specialState = CRBlocks.COPYCAT_HEADSTOCK_BARS.getDefaultState()
				.setValue(CopycatHeadstockBarsBlock.FACING, Direction.NORTH)
				.setValue(CopycatHeadstockBarsBlock.UPSIDE_DOWN, false);
			BlockStateModel specialModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(specialState);
			if (specialModel instanceof CopycatHeadstockBarsModel barsModel)
				return barsModel.getMaterialItemQuads(material, random);
		}

		BlockStateModel materialModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(material);
		List<BlockStateModelPart> materialParts = new ArrayList<>();
		materialModel.collectParts(random, materialParts);

		Direction facing = Direction.NORTH;
		Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
		Vec3 normalScaled14 = normal.scale(14 / 16f);
		List<BakedQuad> quads = new ArrayList<>();
		for (BlockStateModelPart part : materialParts) {
			addCroppedHeadstockQuads(facing, false, normal, normalScaled14, part.getQuads(null), quads::add);
			for (Direction direction : Iterate.directions)
				addCroppedHeadstockQuads(
					facing,
					false,
					normal,
					normalScaled14,
					part.getQuads(direction),
					quads::add
				);
		}
		return List.copyOf(quads);
	}

	private void addHeadstockParts(DirectionData directionData, BlockState state,
								   List<BlockStateModelPart> original, List<BlockStateModelPart> parts) {
		if (original.isEmpty())
			return;

		Direction facing = state.getOptionalValue(CopycatHeadstockBlock.FACING).orElse(Direction.NORTH);
		boolean upsideDown = state.getOptionalValue(CopycatHeadstockBlock.UPSIDE_DOWN).orElse(false);
		Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
		Vec3 normalScaled14 = normal.scale(14 / 16f);

		for (BlockStateModelPart part : original) {
			QuadCollection.Builder builder = new QuadCollection.Builder();
			addCroppedHeadstockQuads(facing, upsideDown, normal, normalScaled14, part.getQuads(null), builder::addUnculledFace);
			for (Direction direction : Iterate.directions) {
				addCroppedHeadstockQuads(
					facing,
					upsideDown,
					normal,
					normalScaled14,
					part.getQuads(direction),
					directionData.isUncull(direction)
						? builder::addUnculledFace
						: quad -> builder.addCulledFace(direction, quad)
				);
			}
			parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
		}
	}

	private static void addCroppedHeadstockQuads(Direction facing, boolean upsideDown, Vec3 normal, Vec3 normalScaled14,
											 List<BakedQuad> quads, Consumer<BakedQuad> consumer) {
		int size = quads.size();
		if (size == 0)
			return;

		for (boolean top : Iterate.trueAndFalse) {
			for (boolean front : Iterate.trueAndFalse) {
				Vec3 offset = normal.scale(front ? 0 : -13 / 16f);
				float contract = 16 - (front ? 1 : 2);
				AABB bb = CUBE_AABB.contract(normal.x * contract / 16, 10 / 16d, normal.z * contract / 16);
				if (!front)
					bb = bb.move(normalScaled14);
				if (top)
					bb = bb.move(0, 10 / 16d, 0);
				else
					offset = offset.add(0, 4 / 16d, 0);
				if (upsideDown)
					offset = offset.add(0, -4 / 16d, 0);

				for (int i = 0; i < size; i++) {
					BakedQuad quad = quads.get(i);
					Direction direction = quad.direction();

					if (front && direction == facing)
						continue;
					if (!front && direction == facing.getOpposite())
						continue;
					if (top && direction == Direction.DOWN)
						continue;
					if (!top && direction == Direction.UP)
						continue;

					consumer.accept(BakedModelHelper.cropAndMove(quad, bb, offset));
				}
			}
		}
	}

	/**
	 * The 26.2 item pipeline no longer asks the block baked model to emit item
	 * quads. Keep the normal item-model transform/properties, but rebuild the
	 * copycat section from the material stored in the block-entity component.
	 */
	public static final class CopycatHeadstockItemModel implements ItemModel {
		private final List<ItemTintSource> itemTints;
		private final QuadCollection wrappedQuads;
		private final Supplier<Vector3fc[]> extents;
		private final ModelRenderProperties properties;
		private final Matrix4fc transformation;
		private final Map<BlockState, List<BakedQuad>> materialQuads = new HashMap<>();

		private CopycatHeadstockItemModel(
			List<ItemTintSource> itemTints,
			QuadCollection wrappedQuads,
			ModelRenderProperties properties,
			Matrix4fc transformation
		) {
			this.itemTints = itemTints;
			this.wrappedQuads = wrappedQuads;
			this.properties = properties;
			this.transformation = transformation;
			// The source copycat quads already describe the final headstock bounds,
			// even though they are replaced at render time.
			extents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(wrappedQuads.getAll()));
		}

		@Override
		public void update(
			ItemStackRenderState output,
			ItemStack stack,
			ItemModelResolver resolver,
			ItemDisplayContext displayContext,
			@Nullable ClientLevel level,
			@Nullable ItemOwner owner,
			int seed
		) {
			ItemData data = ItemData.from(stack);
			UnaryOperator<TextureAtlasSprite> colorSwapper = data.color() == null ? null : getSwapper(data.color());
			List<BakedQuad> source = colorSwapper == null
				? wrappedQuads.getAll()
				: BakedModelHelper.swapSprites(wrappedQuads.getAll(), colorSwapper);

			List<BakedQuad> renderQuads = new ArrayList<>(source.size() + 32);
			for (BakedQuad quad : source) {
				if (!isCopycatBase(quad))
					renderQuads.add(quad);
			}
			List<BakedQuad> copycatQuads = materialQuads.computeIfAbsent(
				data.material(),
				material -> getItemMaterialQuads(material, RandomSource.create(42L))
			);
			renderQuads.addAll(copycatQuads);

			output.appendModelIdentityElement(this);
			output.appendModelIdentityElement(data.material());
			if (data.color() != null)
				output.appendModelIdentityElement(data.color());

			LayerRenderState layer = ItemModelRenderHelper.submitQuads(
				output,
				properties,
				displayContext,
				renderQuads
			);
			BlockStateModel materialModel = Minecraft.getInstance().getModelManager()
				.getBlockStateModelSet()
				.get(data.material());
			layer.setParticleMaterial(materialModel.particleMaterial());

			if (stack.hasFoil()) {
				layer.setFoilType(FoilType.STANDARD);
				output.setAnimated();
				output.appendModelIdentityElement(FoilType.STANDARD);
			} else if (wrappedQuads.hasMaterialFlag(BakedQuad.FLAG_ANIMATED)
				|| copycatQuads.stream().anyMatch(CopycatHeadstockBlockStateModel::isAnimated)) {
				output.setAnimated();
			}

			applyTints(layer, output, stack, level, owner, data.material());
			layer.setExtents(extents);
			layer.setLocalTransform(transformation);
		}

		private void applyTints(
			LayerRenderState layer,
			ItemStackRenderState output,
			ItemStack stack,
			@Nullable ClientLevel level,
			@Nullable ItemOwner owner,
			BlockState material
		) {
			IntList tintLayers = layer.tintLayers();
			if (!itemTints.isEmpty()) {
				for (ItemTintSource tintSource : itemTints) {
					int tint = tintSource.calculate(stack, level, owner == null ? null : owner.asLivingEntity());
					tintLayers.add(tint);
					output.appendModelIdentityElement(tint);
				}
				return;
			}

			// Copycat material quads retain their block tint indices. Item JSONs for
			// these headstocks have no own tint sources, so use the material's item
			// colour for grass, leaves, and other tinted copycat materials.
			for (BlockTintSource tintSource : Minecraft.getInstance().getBlockColors().getTintSources(material)) {
				int tint = tintSource.color(material);
				tintLayers.add(tint);
				output.appendModelIdentityElement(tint);
			}
		}
	}

	private static boolean isAnimated(BakedQuad quad) {
		return (quad.materialInfo().flags() & BakedQuad.FLAG_ANIMATED) != 0;
	}

	private record ItemData(BlockState material, @Nullable DyeColor color) {
		private static ItemData from(ItemStack stack) {
			BlockState material = AllBlocks.COPYCAT_BASE.defaultBlockState();
			DyeColor color = null;
			TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
			if (data == null)
				return new ItemData(material, null);

			CompoundTag tag = data.copyTagWithoutId();
			if (tag.contains("Material")) {
				material = NbtUtils.readBlockState(
					BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK),
					tag.getCompound("Material").orElse(new CompoundTag())
				);
			}
			if (tag.getInt("Color").isPresent())
				color = DyeColor.byId(tag.getInt("Color").orElse(0));
			return new ItemData(material, color);
		}
	}

	public record UnbakedItemModel(CuboidItemModelWrapper.Unbaked wrapped) implements ItemModel.Unbaked {
		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {
			wrapped.resolveDependencies(resolver);
		}

		@Override
		public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
			ModelBaker baker = context.blockModelBaker();
			ResolvedModel resolvedModel = baker.getModel(wrapped.model());
			TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
			QuadCollection quads = resolvedModel.bakeTopGeometry(textureSlots, baker, BlockModelRotation.IDENTITY);
			ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(
				baker,
				resolvedModel,
				textureSlots
			);
			Matrix4fc modelTransform = Transformation.compose(transformation, wrapped.transformation());
			return new CopycatHeadstockItemModel(wrapped.tints(), quads, properties, modelTransform);
		}

		@Override
		public MapCodec<? extends ItemModel.Unbaked> type() {
			return wrapped.type();
		}
	}
}
