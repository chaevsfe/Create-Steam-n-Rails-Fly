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

package com.railwayteam.railways.content.custom_tracks.monorail;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.bogey_menu.handler.BogeyMenuHandlerServer;
import com.railwayteam.railways.content.custom_bogeys.special.monobogey.AbstractMonoBogeyBlock;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.railwayteam.railways.registry.CRShapes;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.trains.bogey.AllBogeySizes;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import com.zurrtum.create.content.trains.graph.TrackEdge;
import com.zurrtum.create.content.trains.graph.TrackGraphHelper;
import com.zurrtum.create.content.trains.graph.TrackGraphLocation;
import com.zurrtum.create.content.trains.graph.TrackNode;
import com.zurrtum.create.content.trains.track.TrackBlock;
import com.zurrtum.create.content.trains.track.TrackMaterial;
import com.zurrtum.create.content.trains.track.TrackPropagator;
import com.zurrtum.create.content.trains.track.TrackShape;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MonorailTrackBlock extends TrackBlock {
    public MonorailTrackBlock(Properties properties, TrackMaterial material) {
        super(properties, material);
    }
    public BlockState getBogeyAnchor(BlockGetter world, BlockPos pos, BlockState state) {
        BlockEntry<? extends AbstractMonoBogeyBlock<?>> block = CRBlocks.MONO_BOGEY;
        if (BogeyMenuHandlerServer.getCurrentPlayer() != null) {
            var styleData = BogeyMenuHandlerServer.getStyle(BogeyMenuHandlerServer.getCurrentPlayer());
            if (styleData.getFirst() == CRBogeyStyles.INVISIBLE || styleData.getFirst() == CRBogeyStyles.INVISIBLE_MONOBOGEY)
                block = CRBlocks.INVISIBLE_MONO_BOGEY;
        }
        return block.getDefaultState()
            .setValue(BlockStateProperties.HORIZONTAL_AXIS, state.getValue(SHAPE) == TrackShape.XO ? Direction.Axis.X : Direction.Axis.Z);
    }
    public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos,
                                        CollisionContext pContext) {
        return switch (pState.getValue(SHAPE)) {
            case AE, AW, AN, AS -> Shapes.empty();
            default -> CRShapes.MONORAIL_COLLISION;
        };
    }
    public VoxelShape getShape(BlockState state, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return getFullShape(state);
    }
    public VoxelShape getInteractionShape(BlockState state, BlockGetter pLevel, BlockPos pPos) {
        return getFullShape(state);
    }

    private VoxelShape getFullShape(BlockState state) {
        switch (state.getValue(SHAPE)) {
            case AE -> {
                return CRShapes.MONORAIL_TRACK_ASC.get(Direction.EAST);
            }
            case AW -> {
                return CRShapes.MONORAIL_TRACK_ASC.get(Direction.WEST);
            }
            case AN -> {
                return CRShapes.MONORAIL_TRACK_ASC.get(Direction.NORTH);
            }
            case AS -> {
                return CRShapes.MONORAIL_TRACK_ASC.get(Direction.SOUTH);
            }
            case CR_D -> {
                return CRShapes.MONORAIL_TRACK_CROSS_DIAG;
            }
            case CR_NDX -> {
                return CRShapes.MONORAIL_TRACK_CROSS_ORTHO_DIAG.get(Direction.SOUTH);
            }
            case CR_NDZ -> {
                return CRShapes.MONORAIL_TRACK_CROSS_DIAG_ORTHO.get(Direction.SOUTH);
            }
            case CR_O -> {
                return CRShapes.MONORAIL_TRACK_CROSS;
            }
            case CR_PDX -> {
                return CRShapes.MONORAIL_TRACK_CROSS_DIAG_ORTHO.get(Direction.EAST);
            }
            case CR_PDZ -> {
                return CRShapes.MONORAIL_TRACK_CROSS_ORTHO_DIAG.get(Direction.EAST);
            }
            case ND -> {
                return CRShapes.MONORAIL_TRACK_DIAG.get(Direction.SOUTH);
            }
            case PD -> {
                return CRShapes.MONORAIL_TRACK_DIAG.get(Direction.EAST);
            }
            case XO -> {
                return CRShapes.MONORAIL_TRACK_ORTHO.get(Direction.EAST);
            }
            case ZO -> {
                return CRShapes.MONORAIL_TRACK_ORTHO.get(Direction.SOUTH);
            }
            case TE -> {
                return CRShapes.MONORAIL_TRACK_ORTHO_LONG.get(Direction.EAST);
            }
            case TW -> {
                return CRShapes.MONORAIL_TRACK_ORTHO_LONG.get(Direction.WEST);
            }
            case TS -> {
                return CRShapes.MONORAIL_TRACK_ORTHO_LONG.get(Direction.SOUTH);
            }
            case TN -> {
                return CRShapes.MONORAIL_TRACK_ORTHO_LONG.get(Direction.NORTH);
            }
            default -> {
            }
        }
        return CRShapes.MONORAIL_TRACK_FALLBACK;
    }
    @Environment(EnvType.CLIENT)
    public PartialModel prepareAssemblyOverlay(BlockGetter world, BlockPos pos, BlockState state, Direction direction,
                                               PoseStack ms) {
        TransformStack.of(ms)
            .rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(direction)), Direction.UP)
            .translateY(14/16f);
        return CRBlockPartials.MONORAIL_TRACK_ASSEMBLING_OVERLAY;
    }
    @SuppressWarnings("deprecation") // deprecated to call, fine to implement
    public void randomTick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!state.hasProperty(SHAPE)) return;
        TrackGraphLocation location = TrackGraphHelper.getGraphLocationAt(level, pos,
            Direction.AxisDirection.POSITIVE, state.getValue(SHAPE).getAxes().get(0));
        if (location == null) return;
        Couple<TrackNode> nodes = location.edge.map((e) -> location.graph.locateNode(e));
        if (nodes.either(Objects::isNull)) return;
        TrackEdge edge = location.graph.getConnection(nodes);
        if (edge == null) return;
        if (edge.getTrackMaterial() != getMaterial())
            TrackPropagator.onRailAdded(level, pos, state);
    }
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = InteractionResult.PASS;
        if (result.consumesAction())
            return result;

        if (!world.isClientSide() && player.getItemInHand(hand).is(AllItems.BRASS_HAND)) {
            TrackPropagator.onRailAdded(world, pos, state);
            return InteractionResult.SUCCESS;
        }
        return result;
    }
}
