/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.content.custom_tracks.generic_crossing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.registry.CRBlockEntities;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.content.trains.graph.TrackNodeLocation;
import com.zurrtum.create.infrastructure.component.BezierTrackPointLocation;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackPropagator;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.zurrtum.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.Affine;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.zurrtum.create.AllShapes.TRACK_ASC;
import static com.zurrtum.create.AllShapes.TRACK_CROSS;
import static com.zurrtum.create.AllShapes.TRACK_CROSS_DIAG;
import static com.zurrtum.create.AllShapes.TRACK_CROSS_DIAG_ORTHO;
import static com.zurrtum.create.AllShapes.TRACK_CROSS_ORTHO_DIAG;
import static com.zurrtum.create.AllShapes.TRACK_DIAG;
import static com.zurrtum.create.AllShapes.TRACK_ORTHO;
import static com.zurrtum.create.AllShapes.TRACK_ORTHO_LONG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GenericCrossingBlock extends Block implements IBE<GenericCrossingBlockEntity>, ITrackBlock, IWrenchable, SpecialBlockItemRequirement, ProperWaterloggedBlock {

    public static final EnumProperty<TrackShape> SHAPE = TrackBlock.SHAPE;

    public GenericCrossingBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SHAPE, TrackShape.CR_O).setValue(WATERLOGGED, false));
    }
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(SHAPE, WATERLOGGED);
    }

    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withWater(super.getStateForPlacement(context), context);
    }

    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        updateWater(level, level, state, currentPos);
        return state;
    }
    public Vec3 getUpNormal(BlockGetter world, BlockPos pos, BlockState state) {
        return state.getValue(SHAPE).getNormal();
    }
    public List<Vec3> getTrackAxes(BlockGetter world, BlockPos pos, BlockState state) {
        return state.getValue(SHAPE).getAxes();
    }
    public Vec3 getCurveStart(BlockGetter world, BlockPos pos, BlockState state, Vec3 axis) {
        boolean vertical = axis.y != 0;
        return VecHelper.getCenterOf(pos)
            .add(0, (vertical ? 0 : -.5f), 0)
            .add(axis.scale(.5));
    }
    public BlockState getBogeyAnchor(BlockGetter world, BlockPos pos, BlockState state) {
        return Blocks.AIR.defaultBlockState();
    }

    @Override // just used by stations for assembly
    public boolean trackEquals(BlockState state1, BlockState state2) {
        return false;
    }
    @Environment(EnvType.CLIENT)
    public <Self extends Affine<Self>> PartialModel prepareTrackOverlay(Affine<Self> affine, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BezierTrackPointLocation bezierTrackPointLocation, AxisDirection axisDirection, RenderedTrackOverlayType renderedTrackOverlayType) {
        return null;
    }
    @Environment(EnvType.CLIENT)
    public PartialModel prepareAssemblyOverlay(BlockGetter world, BlockPos pos, BlockState state, Direction direction, PoseStack ms) {
        return null;
    }
    public TrackMaterial getMaterial() {
        return CRTrackMaterials.PHANTOM;
    }
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.SUCCESS;
    }
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity be) {
        List<ItemStack> stacks = new ArrayList<>();
        return new ItemRequirement(ItemUseType.CONSUME, stacks);
    }
    public Class<GenericCrossingBlockEntity> getBlockEntityClass() {
        return GenericCrossingBlockEntity.class;
    }
    public BlockEntityType<? extends GenericCrossingBlockEntity> getBlockEntityType() {
        return CRBlockEntities.GENERIC_CROSSING.get();
    }

    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        return getFullShape(state);
    }

    @SuppressWarnings("deprecation")
    public VoxelShape getInteractionShape(BlockState state, BlockGetter pLevel, BlockPos pPos) {
        return getFullShape(state);
    }

    private VoxelShape getFullShape(BlockState state) {
        switch (state.getValue(SHAPE)) {
            case AE:
                return TRACK_ASC.get(Direction.EAST);
            case AW:
                return TRACK_ASC.get(Direction.WEST);
            case AN:
                return TRACK_ASC.get(Direction.NORTH);
            case AS:
                return TRACK_ASC.get(Direction.SOUTH);
            case CR_D:
                return TRACK_CROSS_DIAG;
            case CR_NDX:
                return TRACK_CROSS_ORTHO_DIAG.get(Direction.SOUTH);
            case CR_NDZ:
                return TRACK_CROSS_DIAG_ORTHO.get(Direction.SOUTH);
            case CR_O:
                return TRACK_CROSS;
            case CR_PDX:
                return TRACK_CROSS_DIAG_ORTHO.get(Direction.EAST);
            case CR_PDZ:
                return TRACK_CROSS_ORTHO_DIAG.get(Direction.EAST);
            case ND:
                return TRACK_DIAG.get(Direction.SOUTH);
            case PD:
                return TRACK_DIAG.get(Direction.EAST);
            case XO:
                return TRACK_ORTHO.get(Direction.EAST);
            case ZO:
                return TRACK_ORTHO.get(Direction.SOUTH);
            case TE:
                return TRACK_ORTHO_LONG.get(Direction.EAST);
            case TW:
                return TRACK_ORTHO_LONG.get(Direction.WEST);
            case TS:
                return TRACK_ORTHO_LONG.get(Direction.SOUTH);
            case TN:
                return TRACK_ORTHO_LONG.get(Direction.NORTH);
            case NONE:
            default:
        }
        return AllShapes.TRACK_FALLBACK;
    }

    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos,
                                        CollisionContext pContext) {
        return switch (pState.getValue(SHAPE)) {
            case AE, AW, AN, AS -> Shapes.empty();
            default -> AllShapes.TRACK_COLLISION;
        };
    }

    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource p_60465_) {
        TrackPropagator.onRailAdded(level, pos, state);
    }

    @SuppressWarnings("deprecation")
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (pOldState.getBlock() == this)
            return;
        if (pLevel.isClientSide())
            return;
        LevelTickAccess<Block> blockTicks = pLevel.getBlockTicks();
        if (!blockTicks.hasScheduledTick(pPos, this))
            pLevel.scheduleTick(pPos, this, 1);
    }
    public Collection<TrackNodeLocation.DiscoveredLocation> getConnected(BlockGetter worldIn, BlockPos pos, BlockState state,
                                                                         boolean linear, TrackNodeLocation connectedTo) {
        Collection<TrackNodeLocation.DiscoveredLocation> list;
        BlockGetter world = connectedTo != null && worldIn instanceof ServerLevel sl ? sl.getServer()
            .getLevel(connectedTo.dimension) : worldIn;

        if (getTrackAxes(world, pos, state).size() > 1) {
            Vec3 center = Vec3.atBottomCenterOf(pos)
                .add(0, getElevationAtCenter(world, pos, state), 0);
            TrackShape shape = state.getValue(TrackBlock.SHAPE);
            list = new ArrayList<>();
            for (Vec3 axis : getTrackAxes(world, pos, state))
                for (boolean fromCenter : Iterate.trueAndFalse)
                    ITrackBlock.addToListIfConnected(connectedTo, list,
                        (d, b) -> axis.scale(b ? 0 : fromCenter ? -d : d)
                            .add(center),
                        b -> shape.getNormal(), b -> world instanceof Level l ? l.dimension() : Level.OVERWORLD, v -> 0,
                        axis, null, (b, v) -> ITrackBlock.getMaterialSimple(world, v));
        } else
            list = ITrackBlock.super.getConnected(world, pos, state, linear, connectedTo);

        return list;
    }
    public BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        BlockState destroyedState = super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
        if (pLevel.isClientSide())
            return destroyedState;
        if (!pPlayer.isCreative())
            return destroyedState;
        withBlockEntityDo(pLevel, pPos, be -> {
            be.cancelDrops = true;
        });
        return destroyedState;
    }
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
            if (blockEntity instanceof GenericCrossingBlockEntity crossingBE) {
                crossingBE.cancelDrops = true;
                if (!player.isCreative()) {
                    Item a = crossingBE.getPrimary().getBlock().asItem();
                    Item b = crossingBE.getSecondary().getBlock().asItem();
                    player.getInventory().placeItemBackInInventory(new ItemStack(a));
                    player.getInventory().placeItemBackInInventory(new ItemStack(b));
                }
            }
        }

        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof GenericCrossingBlockEntity crossingBE && !crossingBE.cancelDrops) {
                Item a = crossingBE.getPrimary().getBlock().asItem();
                Item b = crossingBE.getSecondary().getBlock().asItem();
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(a));
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(b));
            }

            TrackPropagator.onRailRemoved(level, pos, state);
            level.removeBlockEntity(pos);
        }
    }
}
