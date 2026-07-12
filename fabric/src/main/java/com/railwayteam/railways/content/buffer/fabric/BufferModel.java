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
 */

package com.railwayteam.railways.content.buffer.fabric;

import com.google.common.base.Suppliers;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.railwayteam.railways.content.buffer.IDyedBuffer;
import com.railwayteam.railways.content.buffer.IMaterialAdaptingBuffer;
import com.railwayteam.railways.content.buffer.TrackBufferBlock;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.client.infrastructure.model.WrapperBlockStateModel;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.railwayteam.railways.content.buffer.BufferModelUtils.combineSwappers;
import static com.railwayteam.railways.content.buffer.BufferModelUtils.getSwapper;

@Environment(EnvType.CLIENT)
public class BufferModel extends WrapperBlockStateModel {
    private static final Matrix3f DIAGONAL_TRANSFORM = Axis.YP.rotationDegrees(45).get(new Matrix3f());

    public BufferModel(BlockState state, BlockStateModel.UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        SwapData swapData = SwapData.from(world.getBlockEntity(pos));
        UnaryOperator<TextureAtlasSprite> swapper = swapData.swapper();
        BlockStateModel source = swapper == null ? model : BakedModelHelper.generateModel(model, swapper);

        if (!(state.getBlock() instanceof TrackBufferBlock<?>) || !state.getValue(TrackBufferBlock.DIAGONAL)) {
            source.collectParts(random, parts);
            return;
        }

        List<BlockStateModelPart> sourceParts = new ArrayList<>();
        source.collectParts(random, sourceParts);
        for (BlockStateModelPart part : sourceParts)
            parts.add(rotatePart(part));
    }

    @Override
    public boolean needUpdateTerrainParticle() {
        return true;
    }

    @Override
    public Material.Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        Material.Baked particle = model.particleMaterial();
        UnaryOperator<TextureAtlasSprite> swapper = SwapData.from(world.getBlockEntity(pos)).swapper();
        if (swapper == null)
            return particle;
        TextureAtlasSprite replacement = swapper.apply(particle.sprite());
        return replacement == null ? particle : new Material.Baked(replacement, particle.forceTranslucent());
    }

    private static BlockStateModelPart rotatePart(BlockStateModelPart part) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : part.getQuads(null))
            builder.addUnculledFace(rotateQuad(quad));
        for (Direction direction : Direction.values()) {
            for (BakedQuad quad : part.getQuads(direction))
                builder.addCulledFace(direction, rotateQuad(quad));
        }
        return new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleMaterial());
    }

    private static BakedQuad rotateQuad(BakedQuad quad) {
        Vector3f normal = new Vector3f(
            quad.direction().getStepX(),
            quad.direction().getStepY(),
            quad.direction().getStepZ()
        ).mulTranspose(DIAGONAL_TRANSFORM);

        BakedQuad rotated = new BakedQuad(
            rotatePosition(quad.position0()),
            rotatePosition(quad.position1()),
            rotatePosition(quad.position2()),
            rotatePosition(quad.position3()),
            quad.packedUV0(),
            quad.packedUV1(),
            quad.packedUV2(),
            quad.packedUV3(),
            quad.direction(),
            quad.materialInfo()
        );
        BakedModelHelper.setNormals(rotated, new Vector3f[] {
            new Vector3f(normal), new Vector3f(normal), new Vector3f(normal), new Vector3f(normal)
        });
        return rotated;
    }

    private static Vector3f rotatePosition(Vector3fc position) {
        return new Vector3f(position)
            .sub(0.5f, 0.5f, 0.5f)
            .mulTranspose(DIAGONAL_TRANSFORM)
            .add(0.5f, 0.5f, 0.5f);
    }

    private record SwapData(@Nullable BlockState material, @Nullable DyeColor color) {
        static SwapData from(@Nullable BlockEntity blockEntity) {
            BlockState material = blockEntity instanceof IMaterialAdaptingBuffer adapting
                ? adapting.getMaterial()
                : null;
            DyeColor color = blockEntity instanceof IDyedBuffer dyed ? dyed.getColor() : null;
            return new SwapData(material, color);
        }

        static SwapData from(ItemStack stack) {
            TypedEntityData<?> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data == null)
                return new SwapData(null, null);

            CompoundTag tag = data.copyTagWithoutId();
            BlockState material = tag.contains("Material")
                ? NbtUtils.readBlockState(
                    BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK),
                    tag.getCompound("Material").orElse(new CompoundTag())
                )
                : null;
            DyeColor color = tag.getInt("Color").isPresent()
                ? DyeColor.byId(tag.getInt("Color").orElse(0))
                : null;
            return new SwapData(material, color);
        }

        @Nullable UnaryOperator<TextureAtlasSprite> swapper() {
            if (material == null && color == null)
                return null;
            return combineSwappers(getSwapper(material), getSwapper(color));
        }
    }

    /** Item-model equivalent of the dynamic block wrapper above. */
    public static final class BufferItemModel implements ItemModel {
        private final List<ItemTintSource> tints;
        private final QuadCollection quads;
        private final Supplier<Vector3fc[]> extents;
        private final ModelRenderProperties properties;
        private final Matrix4fc transformation;

        private BufferItemModel(
            List<ItemTintSource> tints,
            QuadCollection quads,
            ModelRenderProperties properties,
            Matrix4fc transformation
        ) {
            this.tints = tints;
            this.quads = quads;
            this.properties = properties;
            this.transformation = transformation;
            extents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(quads.getAll()));
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
            SwapData swapData = SwapData.from(stack);
            UnaryOperator<TextureAtlasSprite> swapper = swapData.swapper();
            List<BakedQuad> renderQuads = swapper == null
                ? quads.getAll()
                : BakedModelHelper.swapSprites(quads.getAll(), swapper);

            output.appendModelIdentityElement(this);
            if (swapData.material() != null)
                output.appendModelIdentityElement(swapData.material());
            if (swapData.color() != null)
                output.appendModelIdentityElement(swapData.color());

            LayerRenderState layer = ItemModelRenderHelper.submitQuads(
                output,
                properties,
                displayContext,
                renderQuads
            );
            if (swapper != null) {
                Material.Baked particle = properties.particleMaterial();
                TextureAtlasSprite replacement = swapper.apply(particle.sprite());
                if (replacement != null)
                    layer.setParticleMaterial(new Material.Baked(replacement, particle.forceTranslucent()));
            }

            if (stack.hasFoil()) {
                layer.setFoilType(FoilType.STANDARD);
                output.setAnimated();
                output.appendModelIdentityElement(FoilType.STANDARD);
            } else if (quads.hasMaterialFlag(BakedQuad.FLAG_ANIMATED)) {
                output.setAnimated();
            }

            if (!tints.isEmpty()) {
                IntList tintLayers = layer.tintLayers();
                for (ItemTintSource tintSource : tints) {
                    int tint = tintSource.calculate(stack, level, owner == null ? null : owner.asLivingEntity());
                    tintLayers.add(tint);
                    output.appendModelIdentityElement(tint);
                }
            }

            layer.setExtents(extents);
            layer.setLocalTransform(transformation);
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
            return new BufferItemModel(wrapped.tints(), quads, properties, modelTransform);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return wrapped.type();
        }
    }
}
