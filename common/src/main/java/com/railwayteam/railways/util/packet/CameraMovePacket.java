package com.railwayteam.railways.util.packet;

import com.google.common.primitives.Floats;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.multiloader.C2SPacket;
import com.railwayteam.railways.multiloader.S2CPacket;
import com.railwayteam.railways.registry.CRPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;


public class CameraMovePacket implements C2SPacket, S2CPacket {
    final int id;
    final double x, y, z;
    final float yaw, pitch;
    final boolean onGround;

    public CameraMovePacket(ConductorEntity entity, double x, double y, double z,
                            float yaw, float pitch, boolean onGround) {
        this.id = entity.getId();
        this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
        this.onGround = onGround;
    }

    public CameraMovePacket(FriendlyByteBuf buf) {
        this.id = buf.readVarInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.yaw = buf.readFloat();
        this.pitch = buf.readFloat();
        this.onGround = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(id);
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
        buffer.writeFloat(yaw);
        buffer.writeFloat(pitch);
        buffer.writeBoolean(onGround);
    }

    @Override
    public void handle(Minecraft mc) {
        ClientPacketHandlers.handleCameraMove(mc, id, x, y, z, yaw, pitch, onGround);
    }

    private static boolean containsInvalidValues(double x, double y, double z, float yaw, float pitch) {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)
                || !Floats.isFinite(pitch) || !Floats.isFinite(yaw);
    }

    private static double clampHorizontal(double value) { return Mth.clamp(value, -3.0E7, 3.0E7); }
    private static double clampVertical(double value)   { return Mth.clamp(value, -2.0E7, 2.0E7); }

    private static boolean isPlayerCollidingWithAnythingNew(ConductorEntity conductor,
                                                             LevelReader world, AABB box) {
        Iterable<VoxelShape> iterable = world.getCollisions(conductor, conductor.getBoundingBox().deflate(1.0E-5f));
        VoxelShape shape = Shapes.create(box.deflate(1.0E-5f));
        for (VoxelShape s : iterable) {
            if (Shapes.joinIsNotEmpty(s, shape, BooleanOp.AND)) continue;
            return true;
        }
        return false;
    }

    public static void teleport(ServerPlayer player, ConductorEntity conductor,
                                double x, double y, double z, float yaw, float pitch) {
        conductor.setPos(x, y, z);
        conductor.setYRot(yaw);
        conductor.setXRot(pitch);
        CRPackets.PACKETS.sendTo(player, new CameraMovePacket(
                conductor, x, y, z, yaw, pitch, conductor.onGround()));
    }

    @Override
    public void handle(ServerPlayer sender) {
        if (!(sender.level().getEntity(id) instanceof ConductorEntity conductor)) return;
        if (sender.getCamera() != conductor) return;
        if (containsInvalidValues(x, y, z, yaw, pitch)) {
            sender.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
            return;
        }
        if (!(conductor.level() instanceof ServerLevel serverLevel)) return;

        double cx = clampHorizontal(x);
        double cy = clampVertical(y);
        double cz = clampHorizontal(z);
        float gy = Mth.wrapDegrees(yaw);
        float gx = Mth.wrapDegrees(pitch);

        if (conductor.isPassenger()) {
            conductor.setYRot(gy);
            conductor.setXRot(gx);
            return;
        }

        double ix = conductor.getX(), iy = conductor.getY(), iz = conductor.getZ();
        double ly = conductor.getY();

        double dm = cx - conductor.firstGoodX;
        double dn = cy - conductor.firstGoodY;
        double doZ = cz - conductor.firstGoodZ;
        double dp = conductor.getDeltaMovement().lengthSqr();
        double dq = dm * dm + dn * dn + doZ * doZ;

        ++conductor.receivedMovePacketCount;
        int r = conductor.receivedMovePacketCount - conductor.knownMovePacketCount;
        if (r > 5) {
            Railways.LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)",
                    sender.getName().getString(), r);
            r = 1;
        }

        float s = conductor.isFallFlying() ? 300.0f : 100.0f;
        if (dq - dp > (double) (s * (float) r)) {
            Railways.LOGGER.warn("{} moved too quickly! {},{},{}", sender.getName().getString(), dm, dn, doZ);
            teleport(sender, conductor, conductor.getX(), conductor.getY(), conductor.getZ(),
                    conductor.getYRot(), conductor.getXRot());
            return;
        }

        AABB aABB = conductor.getBoundingBox();
        dm = cx - conductor.lastGoodX;
        dn = cy - conductor.lastGoodY;
        doZ = cz - conductor.lastGoodZ;
        boolean climbing = dn > 0.0;
        if (conductor.onGround() && !onGround && climbing)
            conductor.jumpFromGround();

        conductor.move(MoverType.PLAYER, new Vec3(dm, dn, doZ));
        dm = cx - conductor.getX();
        dn = cy - conductor.getY();
        if (dn > -0.5 || dn < 0.5) dn = 0.0;
        doZ = cz - conductor.getZ();
        double sq = dm * dm + dn * dn + doZ * doZ;

        if (sq > 0.0625) {
            // moved wrongly, silently discard (client will resync)
            return;
        }

        conductor.setPos(cx, cy, cz);
        conductor.setYRot(gy);
        conductor.setXRot(gx);

        if (!conductor.noPhysics
                && (sq > 0 && serverLevel.noCollision(conductor, aABB)
                || isPlayerCollidingWithAnythingNew(conductor, serverLevel, aABB))) {
            teleport(sender, conductor, ix, iy, iz, gy, gx);
            return;
        }

        conductor.doCheckFallDamage(conductor.getY() - ly, onGround);
        conductor.setOnGround(onGround);
        if (climbing) conductor.resetFallDistance();
        conductor.lastGoodX = conductor.getX();
        conductor.lastGoodY = conductor.getY();
        conductor.lastGoodZ = conductor.getZ();
    }
}
