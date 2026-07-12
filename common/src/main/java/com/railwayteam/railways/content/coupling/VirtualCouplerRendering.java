package com.railwayteam.railways.content.coupling;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class VirtualCouplerRendering {
    private static final double MAX_VIRTUAL_COUPLING_DISTANCE = 64;

    private VirtualCouplerRendering() {}

    public static @Nullable CouplerRenderState createRenderState(Direction direction, double couplingDistance,
                                                                  boolean front, int light,
                                                                  BlockState bogeyState) {
        if (direction == null || !Double.isFinite(couplingDistance) || couplingDistance <= 0
            || couplingDistance > MAX_VIRTUAL_COUPLING_DISTANCE)
            return null;
        if (!(bogeyState.getBlock() instanceof AbstractBogeyBlock<?> bogeyBlock))
            return null;

        Vec3 anchor = bogeyBlock.getConnectorAnchorOffset(false)
            .multiply(front ? -1 : 1, 1, front ? -1 : 1);
        Vec3 anchor2 = anchor.add(Vec3.atLowerCornerOf(direction.getUnitVec3i()).scale(couplingDistance));

        double diffX = anchor2.x - anchor.x;
        double diffY = anchor2.y - anchor.y;
        double diffZ = anchor2.z - anchor.z;
        float yRot = AngleHelper.deg(Mth.atan2(diffZ, diffX)) + 90;
        float xRot = AngleHelper.deg(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)));

        BlockState air = Blocks.AIR.defaultBlockState();
        SuperByteBufferRenderState head = CachedBuffers.partial(AllPartialModels.TRAIN_COUPLING_HEAD, air)
            .light(light)
            .extractRenderState();
        SuperByteBufferRenderState cable = CachedBuffers.partial(AllPartialModels.TRAIN_COUPLING_CABLE, air)
            .light(light)
            .extractRenderState();
        return new CouplerRenderState(anchor, anchor2, yRot, xRot,
            Math.max(0, (int) Math.round(couplingDistance * 4)), head, cable);
    }

    public static void submitCoupler(Direction direction, double couplingDistance, boolean front,
                                     PoseStack matrices, SubmitNodeCollector queue, int light,
                                     BlockState bogeyState) {
        CouplerRenderState state = createRenderState(direction, couplingDistance, front, light, bogeyState);
        if (state != null)
            state.submit(matrices, queue);
    }

    public record CouplerRenderState(Vec3 anchor, Vec3 anchor2, float yRot, float xRot, int couplingSegments,
                                     SuperByteBufferRenderState head, SuperByteBufferRenderState cable) {
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            matrices.translate(anchor.x, anchor.y, anchor.z);

            matrices.pushPose();
            matrices.mulPose(Axis.YP.rotationDegrees(-yRot));
            matrices.mulPose(Axis.XP.rotationDegrees(xRot));
            head.submit(matrices, queue);
            matrices.popPose();

            if (couplingSegments > 0) {
                matrices.pushPose();
                matrices.mulPose(Axis.YP.rotationDegrees(-yRot + 180));
                matrices.mulPose(Axis.XP.rotationDegrees(-xRot));
                matrices.translate(0, 0, 5 / 16f);
                cable.submit(matrices, queue);
                for (int segment = 1; segment < couplingSegments; segment++) {
                    matrices.translate(0, 0, 1 / 4f);
                    cable.submit(matrices, queue);
                }
                matrices.popPose();
            }

            matrices.translate(anchor2.x - anchor.x, anchor2.y - anchor.y, anchor2.z - anchor.z);
            matrices.mulPose(Axis.YP.rotationDegrees(-yRot + 180));
            matrices.mulPose(Axis.XP.rotationDegrees(-xRot));
            head.submit(matrices, queue);
            matrices.popPose();
        }
    }
}
