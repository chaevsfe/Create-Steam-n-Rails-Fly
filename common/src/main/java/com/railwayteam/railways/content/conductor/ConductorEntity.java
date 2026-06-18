package com.railwayteam.railways.content.conductor;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.GameProfile;
import com.railwayteam.railways.content.conductor.toolbox.MountedToolbox;
import com.railwayteam.railways.content.conductor.vent.VentBlock;
import com.railwayteam.railways.content.switches.TrackSwitchBlock;
import com.railwayteam.railways.registry.CREntities;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.registry.CRTags;
import com.railwayteam.railways.util.EntityUtils;
import com.railwayteam.railways.util.ItemUtils;
import com.railwayteam.railways.util.Utils;
import com.railwayteam.railways.util.packet.SetCameraViewPacket;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.redstone.link.IRedstoneLinkable;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.zurrtum.create.content.redstone.link.controller.LinkedControllerItem;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

import static com.railwayteam.railways.content.conductor.ConductorPossessionController.*;

public class ConductorEntity extends AbstractGolem {

    @SuppressWarnings("ConstantValue")
    public static boolean isPlayerDisguised(Player player) {
        if (player.getInventory() == null) return false;
        ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
        if (headStack.isEmpty()) return false;
        if (!CRTags.AllItemTags.CONDUCTOR_CAPS.matches(headStack)) return false;
        String hoverName = headStack.getHoverName().getString();
        return hoverName.startsWith("[sus]") || hoverName.equals("sus");
    }

    public static final GameProfile FAKE_PLAYER_PROFILE = new GameProfile(
            UUID.fromString("B0FADEE5-4411-3475-ADD0-C4EA7E30D050"),
            "[Conductor]"
    );

    public static boolean canSpyInteract(BlockState blockState) {
        Block block = blockState.getBlock();
        return blockState.is(BlockTags.BUTTONS) || blockState.is(BlockTags.TRAPDOORS) || block instanceof LeverBlock
                || block instanceof VentBlock || CRTags.AllBlockTags.CONDUCTOR_SPY_USABLE.matches(blockState);
    }

    public ItemStack getSecondaryHeadStack() {
        return getJob() == Job.SPY ? new ItemStack(AllItems.GOGGLES) : ItemStack.EMPTY;
    }

    // -------------------------------------------------------------------------
    // Frequency system
    // -------------------------------------------------------------------------

    public class FrequencyListener implements IRedstoneLinkable {
        public final String key;
        public final @Nullable Couple<Frequency> frequency;
        public int receivedStrength = 0;

        public FrequencyListener(String key) {
            this.key = key;
            this.frequency = ConductorEntity.this.frequencies.getByName(key);
            if (frequency != null && !frequency.getFirst().getStack().isEmpty())
                Create.REDSTONE_LINK_NETWORK_HANDLER.addToNetwork(ConductorEntity.this.level(), this);
        }

        @Override public int getTransmittedStrength() { return 0; }
        @Override public void setReceivedStrength(int power) { receivedStrength = power; }
        @Override public boolean isListening() { return true; }
        @Override public boolean isAlive() { return ConductorEntity.this.isAlive() && frequency != null; }
        @Override public Couple<Frequency> getNetworkKey() { return frequency; }
        @Override public BlockPos getLocation() { return ConductorEntity.this.blockPosition(); }
        public boolean isPowered() { return receivedStrength > 0; }
    }

    private final FrequencyHolder frequencies = new FrequencyHolder();

    public class FrequencyHolder implements Iterable<Optional<Couple<@NotNull Frequency>>> {
        public FrequencyHolder() {}

        @NotNull @Override
        public Iterator<Optional<Couple<@NotNull Frequency>>> iterator() {
            return List.of(
                    Optional.ofNullable(getForward()), Optional.ofNullable(getBackward()),
                    Optional.ofNullable(getLeft()), Optional.ofNullable(getRight()),
                    Optional.ofNullable(getJump()), Optional.ofNullable(getSneak())
            ).iterator();
        }

        public Map<String, Optional<Couple<@NotNull Frequency>>> entries() {
            return ImmutableMap.of(
                    "forward", Optional.ofNullable(getForward()),
                    "backward", Optional.ofNullable(getBackward()),
                    "left", Optional.ofNullable(getLeft()),
                    "right", Optional.ofNullable(getRight()),
                    "jump", Optional.ofNullable(getJump()),
                    "sneak", Optional.ofNullable(getSneak())
            );
        }

        @Nullable public Couple<Frequency> getByName(String name) {
            return switch (name) {
                case "forward" -> getForward();
                case "backward" -> getBackward();
                case "left" -> getLeft();
                case "right" -> getRight();
                case "jump" -> getJump();
                case "sneak" -> getSneak();
                default -> null;
            };
        }

        public Map<String, Consumer<Optional<Couple<@NotNull Frequency>>>> setters() {
            return ImmutableMap.of(
                    "forward", (f) -> setForward(f.orElse(null)),
                    "backward", (f) -> setBackward(f.orElse(null)),
                    "left", (f) -> setLeft(f.orElse(null)),
                    "right", (f) -> setRight(f.orElse(null)),
                    "jump", (f) -> setJump(f.orElse(null)),
                    "sneak", (f) -> setSneak(f.orElse(null))
            );
        }

        public List<Consumer<Optional<Couple<@NotNull Frequency>>>> settersInOrder() {
            return List.of(
                    (f) -> setForward(f.orElse(null)),
                    (f) -> setBackward(f.orElse(null)),
                    (f) -> setLeft(f.orElse(null)),
                    (f) -> setRight(f.orElse(null)),
                    (f) -> setJump(f.orElse(null)),
                    (f) -> setSneak(f.orElse(null))
            );
        }

        private void set(String name, @Nullable Couple<Frequency> value) {
            Couple<ItemStack> stacks = value == null
                    ? Couple.create(ItemStack.EMPTY, ItemStack.EMPTY)
                    : value.map(Frequency::getStack);
            Couple<EntityDataAccessor<ItemStack>> accessors = FREQUENCY_DATA.get(name);
            for (boolean first : Iterate.trueAndFalse)
                ConductorEntity.this.entityData.set(accessors.get(first), stacks.get(first));
        }

        private Couple<Frequency> get(String name) {
            Couple<EntityDataAccessor<ItemStack>> accessors = FREQUENCY_DATA.get(name);
            return accessors.map(ConductorEntity.this.entityData::get).map(Frequency::of);
        }

        @Nullable public Couple<Frequency> getForward() { return get("forward"); }
        public void setForward(@Nullable Couple<Frequency> v) { set("forward", v); }
        @Nullable public Couple<Frequency> getBackward() { return get("backward"); }
        public void setBackward(@Nullable Couple<Frequency> v) { set("backward", v); }
        @Nullable public Couple<Frequency> getLeft() { return get("left"); }
        public void setLeft(@Nullable Couple<Frequency> v) { set("left", v); }
        @Nullable public Couple<Frequency> getRight() { return get("right"); }
        public void setRight(@Nullable Couple<Frequency> v) { set("right", v); }
        @Nullable public Couple<Frequency> getJump() { return get("jump"); }
        public void setJump(@Nullable Couple<Frequency> v) { set("jump", v); }
        @Nullable public Couple<Frequency> getSneak() { return get("sneak"); }
        public void setSneak(@Nullable Couple<Frequency> v) { set("sneak", v); }
    }

    // -------------------------------------------------------------------------
    // Static data
    // -------------------------------------------------------------------------

    public static final WorldAttached<Set<ConductorEntity>> WITH_TOOLBOXES = new WorldAttached<>(w -> new HashSet<>());

    public static final EntityDataAccessor<Byte> COLOR =
            SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.BYTE);
    public static final EntityDataAccessor<BlockPos> BLOCK =
            SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.BLOCK_POS);
    public static final EntityDataAccessor<Integer> JOB =
            SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> HOLDING_SCHEDULES =
            SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.BOOLEAN);

    public static final Map<String, Couple<EntityDataAccessor<ItemStack>>> FREQUENCY_DATA = new HashMap<>();

    static {
        for (String name : List.of("forward", "backward", "left", "right", "jump", "sneak")) {
            FREQUENCY_DATA.put(name, Couple.create(
                    SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.ITEM_STACK),
                    SynchedEntityData.defineId(ConductorEntity.class, EntityDataSerializers.ITEM_STACK)
            ));
        }
    }

    private static final Vec3i REACH = new Vec3i(3, 2, 3);

    // -------------------------------------------------------------------------
    // Instance fields
    // -------------------------------------------------------------------------

    private ServerPlayer fakePlayer = null;
    MountedToolbox toolbox = null;
    private List<ItemStack> heldSchedules;

    // Possession position tracking (based on SecurityCraft, MIT license)
    public double firstGoodX, firstGoodY, firstGoodZ;
    public double lastGoodX, lastGoodY, lastGoodZ;
    public int receivedMovePacketCount;
    public int knownMovePacketCount;

    public double xLast, yLast, zLast, xRotLast, yRotLast;

    public void resetPosition() {
        firstGoodX = lastGoodX = getX();
        firstGoodY = lastGoodY = getY();
        firstGoodZ = lastGoodZ = getZ();
        knownMovePacketCount = receivedMovePacketCount;
    }

    public void doCheckFallDamage(double y, boolean onGround) {
        if (this.touchingUnloadedChunk()) return;
        BlockPos blockPos = this.getOnPos();
        super.checkFallDamage(y, onGround, this.level().getBlockState(blockPos), blockPos);
    }

    @Override
    public void jumpFromGround() {
        super.jumpFromGround();
    }

    public void teleportToForce(double x, double y, double z) {
        this.setPos(x, y, z);
        this.setYRot(this.getYRot());
        this.setXRot(this.getXRot());
        this.getSelfAndPassengers().forEach(entity -> {
            for (Entity e2 : entity.getPassengers()) entity.positionRider(e2);
        });
        if (level() instanceof ServerLevel serverLevel) {
            ChunkPos cp = new ChunkPos(this.blockPosition());
            serverLevel.getChunkSource().addTicketWithRadius(TicketType.FORCED, cp, 3);
        }
        firstGoodX = lastGoodX = x;
        firstGoodY = lastGoodY = y;
        firstGoodZ = lastGoodZ = z;
        // CameraMovePacket sync handled separately when PosRot API is available
    }

    public int ventCooldown = 0;

    public static final List<Player> RECENTLY_DISMOUNTED_PLAYERS = new ArrayList<>();
    @NotNull private WeakReference<ServerPlayer> currentlyViewing = new WeakReference<>(null);
    private int initialChunkLoadingDistance = 0;
    private boolean hasSentChunks = false;

    public SectionPos oldSectionPos = null;

    public static boolean hasRecentlyDismounted(Player player) {
        return RECENTLY_DISMOUNTED_PLAYERS.remove(player);
    }

    public void setChunkLoadingDistance(int d) { initialChunkLoadingDistance = d; }
    public int getChunkLoadingDistance() { return initialChunkLoadingDistance; }
    public boolean hasSentChunks() { return hasSentChunks; }
    public void setHasSentChunks(boolean v) { hasSentChunks = v; }

    // -------------------------------------------------------------------------
    // Client-side possession state
    // -------------------------------------------------------------------------

    public float oBob;
    public float bob;

    // visualBaseModel/visualBaseEntity are not used in the 1.21.11 port since
    // setupAnim uses render-state (ConductorRenderState) instead of entity fields.

    private boolean rotateAnyway = false;
    private boolean consumeRotateAnyway() {
        if (rotateAnyway) { rotateAnyway = false; return true; }
        return false;
    }

    private Float flyingSpeedOverride = null;

    @Override
    protected float getFlyingSpeed() {
        return flyingSpeedOverride == null ? super.getFlyingSpeed() : flyingSpeedOverride;
    }

    public boolean isPossessed() {
        return level().isClientSide() ? ClientHandler.isPossessed(this) : currentlyViewing.get() != null;
    }

    public boolean isPossessedAndClient() {
        return level().isClientSide() && isPossessed();
    }

    @Override
    protected boolean isImmobile() {
        return isPossessedAndClient() ? false : super.isImmobile();
    }

    @Override
    public boolean isEffectiveAi() {
        return isPossessedAndClient() || super.isEffectiveAi();
    }

    @Override
    public boolean canSimulateMovement() {
        // 1.21.11: travel() is only called when canSimulateMovement() AND isEffectiveAi() are both true.
        // isLocalInstanceAuthoritative() (which the default delegates to) returns false for server-
        // controlled mobs on the client, so we must override this separately from isEffectiveAi().
        return isPossessedAndClient() || super.canSimulateMovement();
    }

    public void turnView(double yRot, double xRot) {
        float f = (float) xRot * 0.15f;
        float g = (float) yRot * 0.15f;
        rotateAnyway = true;
        this.setXRot(this.getXRot() + f);
        this.setYRot(this.getYRot() + g);
        rotateAnyway = true;
        this.setXRot(Mth.clamp(this.getXRot(), -90.0f, 90.0f));
        this.xRotO += f;
        this.yRotO += g;
        this.xRotO = Mth.clamp(this.xRotO, -90.0f, 90.0f);
        if (this.getVehicle() != null)
            this.getVehicle().onPassengerTurned(this);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float getViewXRot(float partialTicks) {
        if (ClientHandler.isPossessed(this)) return this.getXRot();
        return super.getViewXRot(partialTicks);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public float getViewYRot(float partialTick) {
        return this.isPassenger() || !isPossessed() ? super.getViewYRot(partialTick) : this.getYRot();
    }

    @Override
    public void setXRot(float xRot) {
        if (isPossessedAndClient() && !consumeRotateAnyway()) return;
        super.setXRot(xRot);
    }

    @Override
    public boolean isCrouching() {
        return level().isClientSide() ? super.isCrouching() : super.isCrouching();
    }

    @Override
    protected boolean isHorizontalCollisionMinor(@NotNull Vec3 deltaMovement) {
        if (!isPossessedAndClient()) return super.isHorizontalCollisionMinor(deltaMovement);
        float f = this.getYRot() * ((float) Math.PI / 180);
        double d0 = Mth.sin(f), d1 = Mth.cos(f);
        double d2 = (double) this.xxa * d1 - (double) this.zza * d0;
        double d3 = (double) this.zza * d1 + (double) this.xxa * d0;
        double d4 = Mth.square(d2) + Mth.square(d3);
        double d5 = Mth.square(deltaMovement.x) + Mth.square(deltaMovement.z);
        if (!(d4 < 1.0E-5f) && !(d5 < 1.0E-5f)) {
            double d6 = d2 * deltaMovement.x + d3 * deltaMovement.z;
            double d7 = Math.acos(d6 / Math.sqrt(d4 * d5));
            return d7 < 0.13962633907794952;
        }
        return false;
    }

    public void updatePossessionInputs() {
        if (isPossessedAndClient()) _updatePossessionInputs();
    }

    private static float calculateImpulse(boolean input, boolean otherInput) {
        if (input == otherInput) return 0.0f;
        return input ? 1.0f : -1.0f;
    }

    @Environment(EnvType.CLIENT)
    private void _updatePossessionInputs() {
        this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED));
        this.zza = calculateImpulse(wasUpPressed(), wasDownPressed());
        this.xxa = calculateImpulse(wasLeftPressed(), wasRightPressed());
        if (!wasSprintPressed()) { zza *= 0.3F; xxa *= 0.3F; }
        this.jumping = wasJumpPressed();
        this.flyingSpeedOverride = 0.2f;
        if (wasSprintPressed()) this.flyingSpeedOverride += 0.006f;
    }

    private boolean suffocatesAt(BlockPos pos) {
        AABB aabb = this.getBoundingBox();
        AABB aabb1 = new AABB(pos.getX(), aabb.minY, pos.getZ(),
                (double) pos.getX() + 1.0, aabb.maxY, (double) pos.getZ() + 1.0).deflate(1.0E-7);
        return this.level().collidesWithSuffocatingBlock(this, aabb1);
    }

    private void moveTowardsClosestSpace(double x, double z) {
        BlockPos blockpos = BlockPos.containing(x, this.getY(), z);
        if (this.suffocatesAt(blockpos)) {
            double d0 = x - blockpos.getX(), d1 = z - blockpos.getZ();
            Direction dir = null;
            double d2 = Double.MAX_VALUE;
            for (Direction d : new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}) {
                double d3 = d.getAxis().choose(d0, 0.0, d1);
                double d4 = d.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0 - d3 : d3;
                if (!(d4 < d2) || this.suffocatesAt(blockpos.relative(d))) continue;
                d2 = d4; dir = d;
            }
            if (dir != null) {
                Vec3 vec3 = this.getDeltaMovement();
                if (dir.getAxis() == Direction.Axis.X)
                    this.setDeltaMovement(0.1 * dir.getStepX(), vec3.y, vec3.z);
                else
                    this.setDeltaMovement(vec3.x, vec3.y, 0.1 * dir.getStepZ());
            }
        }
    }

    @Override
    public void aiStep() {
        if (isPossessedAndClient()) {
            this.oBob = this.bob;
            this.yHeadRot = this.getYRot();
            if (!this.noPhysics) {
                float hw = this.getBbWidth() * 0.35f;
                moveTowardsClosestSpace(this.getX() - hw, this.getZ() + hw);
                moveTowardsClosestSpace(this.getX() - hw, this.getZ() - hw);
                moveTowardsClosestSpace(this.getX() + hw, this.getZ() - hw);
                moveTowardsClosestSpace(this.getX() + hw, this.getZ() + hw);
            }
        }
        super.aiStep();
        if (isPossessedAndClient()) {
            float f = !this.onGround() || this.isDeadOrDying() || this.isSwimming() ? 0.0f
                    : Math.min(0.1f, (float) this.getDeltaMovement().horizontalDistance());
            this.bob += (f - this.bob) * 0.4f;
        }
    }

    @Override
    public void push(@NotNull Entity entity) {
        if (ConductorPossessionController.getPossessingConductor(entity) == this) return;
        super.push(entity);
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !isPossessed();
    }

    // -------------------------------------------------------------------------
    // Frequency listeners
    // -------------------------------------------------------------------------

    protected FrequencyListener forwardListener;
    protected FrequencyListener backwardListener;
    protected FrequencyListener leftListener;
    protected FrequencyListener rightListener;
    protected FrequencyListener jumpListener;
    protected FrequencyListener sneakListener;

    protected void updateFrequencyListeners() {
        forwardListener  = new FrequencyListener("forward");
        backwardListener = new FrequencyListener("backward");
        leftListener     = new FrequencyListener("left");
        rightListener    = new FrequencyListener("right");
        jumpListener     = new FrequencyListener("jump");
        sneakListener    = new FrequencyListener("sneak");
    }

    public int getForwardSignalStrength() {
        return forwardListener == null ? 0 : forwardListener.receivedStrength;
    }

    public int getThrottle() {
        int fwd = forwardListener  == null ? 0 : forwardListener.receivedStrength;
        int bwd = backwardListener == null ? 0 : backwardListener.receivedStrength;
        return fwd - bwd;
    }

    // -------------------------------------------------------------------------
    // Constructor / attributes
    // -------------------------------------------------------------------------

    public ConductorEntity(EntityType<? extends AbstractGolem> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0);
    }

    // -------------------------------------------------------------------------
    // Synced data
    // -------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COLOR, idFrom(defaultColor()));
        builder.define(BLOCK, this.blockPosition());
        builder.define(JOB, Job.DEFAULT.ordinal());
        builder.define(HOLDING_SCHEDULES, false);
        for (Map.Entry<String, Couple<EntityDataAccessor<ItemStack>>> entry : FREQUENCY_DATA.entrySet()) {
            for (boolean first : Iterate.trueAndFalse)
                builder.define(entry.getValue().get(first), ItemStack.EMPTY);
        }
    }

    // -------------------------------------------------------------------------
    // AI goals
    // -------------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(2, new ConductorLookedAtGoal(this));
        goalSelector.addGoal(1, new ConductorPonderBlockGoal(this));
        goalSelector.addGoal(1, new FollowToolboxPlayerGoal(this, 1.25d));
        goalSelector.addGoal(1, new RemoteControlGoal(this, 1.25d));
        goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 8f) {
            @Override public boolean canUse() { return super.canUse() && !isPossessed(); }
            @Override public boolean canContinueToUse() { return super.canContinueToUse() && !isPossessed(); }
        });
    }

    // -------------------------------------------------------------------------
    // Job
    // -------------------------------------------------------------------------

    public enum Job {
        REDSTONE_OPERATOR,
        TOOLBOX_CARRIER,
        REMOTE_CONTROL,
        SPY;
        public static final Job DEFAULT = REDSTONE_OPERATOR;
    }

    public void setJob(Job job) { getEntityData().set(JOB, job.ordinal()); }

    public Job getJob() {
        int idx = getEntityData().get(JOB);
        Job[] vals = Job.values();
        return idx >= 0 && idx < vals.length ? vals[idx] : Job.DEFAULT;
    }

    // -------------------------------------------------------------------------
    // Schedules
    // -------------------------------------------------------------------------

    private List<ItemStack> getHeldSchedules() {
        if (heldSchedules == null) heldSchedules = new ArrayList<>();
        return heldSchedules;
    }

    public boolean isHoldingSchedules() { return !getHeldSchedules().isEmpty(); }
    public boolean isHoldingSchedulesClient() { return this.entityData.get(HOLDING_SCHEDULES); }

    public void addSchedule(ItemStack stack) {
        if (stack.isEmpty()) return;
        getHeldSchedules().add(stack.copy());
        this.entityData.set(HOLDING_SCHEDULES, true);
    }

    // -------------------------------------------------------------------------
    // Toolbox
    // -------------------------------------------------------------------------

    public boolean isCarryingToolbox() { return toolbox != null; }

    @Nullable public MountedToolbox getToolbox() { return toolbox; }

    @NotNull
    public MountedToolbox getOrCreateToolboxHolder() {
        if (!isCarryingToolbox()) setToolbox(new MountedToolbox(this, DyeColor.BROWN));
        return toolbox;
    }

    protected boolean isToolbox(ItemStack stack) {
        return stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ToolboxBlock;
    }

    public void setToolbox(@Nullable MountedToolbox tb) {
        this.toolbox = tb;
        if (tb != null) WITH_TOOLBOXES.get(level()).add(this);
        else WITH_TOOLBOXES.get(level()).remove(this);
    }

    public ItemStack getToolboxDisplayStack() {
        return isCarryingToolbox() ? toolbox.getDisplayStack() : ItemStack.EMPTY;
    }

    public void equipToolbox(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof ToolboxBlock tb) {
            setToolbox(new MountedToolbox(this, tb.getColor()));
            this.toolbox.readFromItem(stack);
            this.toolbox.sendData();
            setJob(Job.TOOLBOX_CARRIER);
        }
    }

    public ItemStack unequipToolbox() {
        setJob(Job.DEFAULT);
        if (level().isClientSide() || toolbox == null) {
            if (toolbox != null) toolbox.setRemoved();
            setToolbox(null);
            return ItemStack.EMPTY;
        }
        toolbox.unequipTracked();
        ItemStack stack = toolbox.getCloneItemStack();
        toolbox.setRemoved();
        setToolbox(null);
        return stack;
    }

    protected void openToolbox(Player player) {
        if (player instanceof ServerPlayer sp) MountedToolbox.openMenu(sp, toolbox);
    }

    @Override
    public void startSeenByPlayer(@NotNull ServerPlayer sp) {
        super.startSeenByPlayer(sp);
        if (toolbox != null) toolbox.sendData();
    }

    // -------------------------------------------------------------------------
    // Color
    // -------------------------------------------------------------------------

    public static DyeColor defaultColor() { return DyeColor.BLUE; }

    public void setColor(DyeColor color) { getEntityData().set(COLOR, idFrom(color)); }

    public DyeColor getColor() { return colorFrom(this.entityData.get(COLOR)); }

    public static DyeColor colorFrom(byte b) {
        if (b >= 16) return null;
        return DyeColor.byId(b);
    }

    public static byte idFrom(DyeColor color) {
        int c = color.getId();
        if (c >= 16) return 16;
        return (byte) c;
    }

    public boolean isCorrectEngineerCap(ItemStack hat) {
        if (hat.isEmpty()) return true;
        return (hat.getItem() instanceof ConductorCapItem cap) && (cap.color == getColor());
    }

    // -------------------------------------------------------------------------
    // Possession / viewing
    // -------------------------------------------------------------------------

    public boolean startViewing(ServerPlayer player) {
        ServerPlayer current = currentlyViewing.get();
        if (current != null && current.getCamera() == this && current.isAlive() && current != player) return false;
        if (player.level() != level()) return false;
        if (player.getCamera() instanceof ConductorEntity c) c.stopViewing(player);
        currentlyViewing = new WeakReference<>(player);
        oldSectionPos = null;
        setChunkLoadingDistance(((ServerLevel) player.level()).getServer().getPlayerList().getViewDistance());
        setHasSentChunks(false);
        player.camera = this;
        CRPackets.PACKETS.sendTo(player, new SetCameraViewPacket(this));
        resetPosition();
        return true;
    }

    public void stopViewing(ServerPlayer player) {
        if (!level().isClientSide()) {
            currentlyViewing.clear();
            player.camera = player;
            CRPackets.PACKETS.sendTo(player, new SetCameraViewPacket(player));
            RECENTLY_DISMOUNTED_PLAYERS.add(player);
        }
    }

    // -------------------------------------------------------------------------
    // Spy interaction
    // -------------------------------------------------------------------------

    @SuppressWarnings("DuplicatedCode")
    public void onSpyInteract(BlockPos pos) {
        BlockState state;
        if (this.canReach(pos) && canSpyInteract((state = this.level().getBlockState(pos))) && fakePlayer != null) {
            ClipContext ctx = new ClipContext(this.getEyePosition(),
                    new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, fakePlayer);
            BlockHitResult hit = level().clip(ctx);
            if (!pos.equals(hit.getBlockPos())) return;
            boolean canUse = state.getShape(level(), pos).isEmpty()
                    || EntityUtils.handleUseEvent(fakePlayer, InteractionHand.MAIN_HAND, hit);
            if (canUse) {
                if (state.getBlock() instanceof VentBlock vb)
                    vb.teleportConductor(level(), pos, this, hit.getDirection().getOpposite());
                else
                    state.useWithoutItem(level(), fakePlayer, hit);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        this.resetPosition();
        SectionPos sectionPos = SectionPos.of(this);
        if (!sectionPos.equals(oldSectionPos)) setHasSentChunks(false);
        if (level().isClientSide()) {
            ConductorPossessionController.tryUpdatePossession(this);
            updatePossessionInputs();
            if (isPossessedAndClient())
                this.getInterpolation().cancel();
        }
        if (level() instanceof ServerLevel serverLevel && fakePlayer == null)
            fakePlayer = EntityUtils.createConductorFakePlayer(serverLevel, this);
        super.tick();
        if (ventCooldown > 0) ventCooldown--;
        if (level() instanceof ServerLevel serverLevel) {
            ServerPlayer player = currentlyViewing.get();
            if (player != null) {
                ChunkPos cp = new ChunkPos(blockPosition());
                int vd = serverLevel.getServer().getPlayerList().getViewDistance();
                for (int x = cp.x - vd; x <= cp.x + vd; x++)
                    for (int z = cp.z - vd; z <= cp.z + vd; z++)
                        serverLevel.getChunkSource().addTicketWithRadius(TicketType.FORCED, new ChunkPos(x, z), 3);
            }
        }
        if (toolbox != null) toolbox.tick();
    }

    // -------------------------------------------------------------------------
    // Spawn helper
    // -------------------------------------------------------------------------

    public static ConductorEntity spawn(Level level, BlockPos pos, ItemStack stack) {
        if (!(stack.getItem() instanceof ConductorCapItem cap)) return null;
        ConductorEntity result = new ConductorEntity(CREntities.CONDUCTOR.get(), level);
        result.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        result.setColor(cap.color);
        result.setItemSlot(EquipmentSlot.HEAD, stack.copyWithCount(1));
        level.addFreshEntity(result);
        return result;
    }

    // -------------------------------------------------------------------------
    // Misc queries
    // -------------------------------------------------------------------------

    public boolean isInMinecart() { return this.getVehicle() instanceof AbstractMinecart; }

    public boolean canReach(Vec3i pos) {
        return pos.distToCenterSqr(position()) <= REACH.distSqr(Vec3i.ZERO);
    }

    boolean isLookingAtMe(Player player) {
        if (player.isSpectator()) return false;
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!isCorrectEngineerCap(helmet) && ItemUtils.blocksEndermanView(helmet, player, null)) return false;
        Vec3 playerView = player.getViewVector(1f).normalize();
        Vec3 headLine   = this.getEyePosition().subtract(player.getEyePosition()).normalize();
        double angle    = playerView.dot(headLine);
        return (angle > 1d - 0.017d) && player.hasLineOfSight(this);
    }

    public boolean canUseBlock(BlockState state) {
        return state.is(BlockTags.BUTTONS) || state.is(Blocks.LEVER)
                || state.getBlock() instanceof TrackSwitchBlock;
    }

    // -------------------------------------------------------------------------
    // Hurt / sound / remove
    // -------------------------------------------------------------------------

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
        WITH_TOOLBOXES.get(level()).remove(this);
    }

    @Override
    public void onClientRemoval() {
        WITH_TOOLBOXES.get(level()).remove(this);
    }

    @Override
    protected int decreaseAirSupply(int air) { return air; }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource src) { return SoundEvents.NETHERITE_BLOCK_BREAK; }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() { return AllSoundEvents.CRUSHING_1.getMainEvent(); }

    // -------------------------------------------------------------------------
    // Interaction
    // -------------------------------------------------------------------------

    @Override
    protected @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof DyeItem di) {
            setColor(di.getDyeColor());
            if (!player.isCreative()) stack.shrink(1);
            return InteractionResult.SUCCESS;
        } else if (stack.getItem() == AllBlocks.ANDESITE_CASING.asItem()) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.setHealth(this.getMaxHealth());
                if (!player.isCreative()) stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        } else if (!this.isCarryingToolbox() && isToolbox(stack) && getJob() == Job.DEFAULT) {
            this.equipToolbox(stack);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        } else if (this.isCarryingToolbox()) {
            if (player.isShiftKeyDown() && stack.isEmpty())
                player.setItemInHand(hand, this.unequipToolbox());
            else if (!level().isClientSide())
                openToolbox(player);
            return InteractionResult.SUCCESS;
        } else if (stack.isEmpty() && !getHeldSchedules().isEmpty()) {
            for (ItemStack item : heldSchedules) {
                if (!player.addItem(item)) player.drop(item, false);
            }
            this.entityData.set(HOLDING_SCHEDULES, false);
            getHeldSchedules().clear();
        } else if (getJob() == Job.DEFAULT && stack.getItem() == AllBlocks.REDSTONE_LINK.asItem()) {
            setJob(Job.REMOTE_CONTROL);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        } else if (getJob() == Job.REMOTE_CONTROL) {
            if (player.isShiftKeyDown() && stack.isEmpty()) {
                player.setItemInHand(hand, new ItemStack(AllBlocks.REDSTONE_LINK));
                setJob(Job.DEFAULT);
            } else if (stack.getItem() instanceof LinkedControllerItem) {
                int i = 0;
                for (var freq : frequencies.settersInOrder()) {
                    freq.accept(Optional.of(LinkedControllerItem.toFrequency(stack, i)));
                    i++;
                }
                updateFrequencyListeners();
            }
        } else if (getJob() == Job.DEFAULT && stack.is(AllItems.GOGGLES)) {
            setJob(Job.SPY);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        } else if (getJob() == Job.SPY) {
            if (player.isShiftKeyDown() && stack.isEmpty()) {
                player.setItemInHand(hand, new ItemStack(AllItems.GOGGLES));
                setJob(Job.DEFAULT);
            }
        }
        return super.mobInteract(player, hand);
    }

    // -------------------------------------------------------------------------
    // Death loot
    // -------------------------------------------------------------------------

    @Override
    protected void dropCustomDeathLoot(@NotNull ServerLevel level, @NotNull DamageSource src, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, src, recentlyHit);
        Job job = getJob();
        ItemStack holding = this.unequipToolbox();
        if (!holding.isEmpty()) this.spawnAtLocation(level, holding);
        if (isHoldingSchedules()) {
            for (ItemStack s : getHeldSchedules()) this.spawnAtLocation(level, s);
        }
        if (job == Job.REMOTE_CONTROL)
            this.spawnAtLocation(level, new ItemStack(AllBlocks.REDSTONE_LINK));
        else if (job == Job.SPY)
            this.spawnAtLocation(level, new ItemStack(AllItems.GOGGLES));
    }

    // -------------------------------------------------------------------------
    // Save / load
    // -------------------------------------------------------------------------

    private static final List<String> FREQ_KEYS =
            List.of("forward", "backward", "left", "right", "jump", "sneak");

    @Override
    protected void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("color", getEntityData().get(COLOR));
        output.putString("job", getJob().name());
        if (toolbox != null)
            output.store("toolboxHolder", net.minecraft.nbt.CompoundTag.CODEC, toolbox.write(false));
        if (!getHeldSchedules().isEmpty()) {
            ValueOutput.TypedOutputList<ItemStack> list = output.list("heldSchedules", ItemStack.OPTIONAL_CODEC);
            for (ItemStack s : heldSchedules) { if (!s.isEmpty()) list.add(s); }
        }
        // Save frequencies as pairs of ItemStacks via codec
        for (String key : FREQ_KEYS) {
            Couple<EntityDataAccessor<ItemStack>> accessors = FREQUENCY_DATA.get(key);
            ItemStack first  = entityData.get(accessors.getFirst());
            ItemStack second = entityData.get(accessors.getSecond());
            if (!first.isEmpty() || !second.isEmpty()) {
                ValueOutput.TypedOutputList<ItemStack> freqList = output.list("freq_" + key, ItemStack.OPTIONAL_CODEC);
                freqList.add(first);
                freqList.add(second);
            }
        }
        // BLOCK target pos
        BlockPos target = getEntityData().get(BLOCK);
        output.putInt("targetX", target.getX());
        output.putInt("targetY", target.getY());
        output.putInt("targetZ", target.getZ());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        getEntityData().set(COLOR, input.getByteOr("color", idFrom(defaultColor())));
        input.getString("job").ifPresent(name -> {
            try { setJob(Job.valueOf(name)); }
            catch (IllegalArgumentException ignored) { setJob(Job.DEFAULT); }
        });
        input.read("toolboxHolder", net.minecraft.nbt.CompoundTag.CODEC)
                .ifPresent(tag -> setToolbox(MountedToolbox.read(this, tag)));
        getHeldSchedules().clear();
        for (ItemStack s : input.listOrEmpty("heldSchedules", ItemStack.OPTIONAL_CODEC)) {
            if (!s.isEmpty()) getHeldSchedules().add(s);
        }
        // Load frequencies — iterate TypedInputList to extract first two stacks
        for (String key : FREQ_KEYS) {
            ItemStack first = ItemStack.EMPTY, second = ItemStack.EMPTY;
            int idx = 0;
            for (ItemStack s : input.listOrEmpty("freq_" + key, ItemStack.OPTIONAL_CODEC)) {
                if (idx == 0) first = s; else if (idx == 1) second = s;
                idx++;
            }
            Couple<EntityDataAccessor<ItemStack>> accessors = FREQUENCY_DATA.get(key);
            entityData.set(accessors.getFirst(),  first);
            entityData.set(accessors.getSecond(), second);
        }
        if (!level().isClientSide()) {
            this.entityData.set(HOLDING_SCHEDULES, isHoldingSchedules());
            updateFrequencyListeners();
        }
        // Restore target block pos
        getEntityData().set(BLOCK, new BlockPos(
                input.getIntOr("targetX", 0),
                input.getIntOr("targetY", 0),
                input.getIntOr("targetZ", 0)));
    }

    // -------------------------------------------------------------------------
    // Inner goal classes
    // -------------------------------------------------------------------------

    static class JobBasedGoal extends Goal {
        private final Job job;
        protected final ConductorEntity conductor;

        public JobBasedGoal(ConductorEntity conductor, Job job) {
            this.conductor = conductor;
            this.job = job;
        }

        @Override public boolean canUse() { return conductor.getJob() == job && !conductor.isPossessed(); }
        @Override public boolean canContinueToUse() { return this.canUse(); }
    }

    static class FollowToolboxPlayerGoal extends JobBasedGoal {
        protected final double speedModifier;
        @Nullable protected Player target;
        protected int timeToRecalcPath;

        public FollowToolboxPlayerGoal(ConductorEntity conductor, double speedModifier) {
            super(conductor, Job.TOOLBOX_CARRIER);
            this.speedModifier = speedModifier;
        }

        @Override
        public void tick() {
            super.tick();
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (this.conductor.distanceToSqr(target) > 16)
                    this.conductor.getNavigation().moveTo(target, this.speedModifier);
                else
                    this.conductor.getNavigation().stop();
            }
        }

        @Override
        public boolean canUse() {
            return super.canUse() && conductor.isCarryingToolbox()
                    && !conductor.getToolbox().getConnectedPlayers().isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && conductor.isCarryingToolbox()
                    && conductor.getToolbox().getConnectedPlayers().contains(target)
                    && target.isAlive() && !target.isSpectator();
        }

        @Override
        public void start() {
            super.start();
            List<Player> players = conductor.getToolbox().getConnectedPlayers();
            target = players.get(conductor.random.nextInt(players.size()));
        }

        @Override
        public void stop() { super.stop(); target = null; }
    }

    static class RemoteControlGoal extends JobBasedGoal {
        protected final double speedModifier;
        protected Vec3 targetDirection = Vec3.ZERO;
        protected double targetStrength = 15.0;
        private int honkPacketCooldown = 0;
        private boolean usedToHonk;

        public RemoteControlGoal(ConductorEntity conductor, double speedModifier) {
            super(conductor, Job.REMOTE_CONTROL);
            this.speedModifier = speedModifier;
        }

        protected double getGroundY(Vec3 vec) {
            BlockPos blockPos = BlockPos.containing(vec);
            return conductor.level().getBlockState(blockPos.below()).isAir() ? vec.y : blockPos.getY();
        }

        @Override
        public void tick() {
            super.tick();
            Pair<Vec3, Double> pair = calculateTarget();
            targetDirection = pair.getFirst();
            targetStrength = pair.getSecond();
            Vec3 target = conductor.position().add(targetDirection.scale(2));

            if (conductor.getRootVehicle() instanceof CarriageContraptionEntity cce
                    && cce.getControllingPlayer().isEmpty()
                    && cce.getContraption() instanceof CarriageContraption cc
                    && conductor.fakePlayer != null) {

                BlockPos seat = cc.getSeatOf(conductor.getUUID());
                if (seat == null) return;
                Couple<Boolean> controlsPresent = cc.conductorSeats.get(seat);
                if (controlsPresent == null) return;

                Carriage carriage = cce.getCarriage();
                if (carriage != null) {
                    ScheduleRuntime runtime = carriage.train.runtime;
                    if (runtime.schedule != null && !(runtime.completed || runtime.paused)) return;
                }

                BlockPos controlsPos = null, reverseControlsPos = null;
                if (controlsPresent.getFirst())
                    controlsPos = seat.relative(cc.getAssemblyDirection().getOpposite());
                if (controlsPresent.getSecond()) {
                    if (controlsPos == null) controlsPos = seat.relative(cc.getAssemblyDirection());
                    else reverseControlsPos = seat.relative(cc.getAssemblyDirection());
                }
                if (controlsPos == null) return;

                Set<Integer> controls = getControls();
                boolean isSprintKeyPressed = controls.remove(5);
                Set<Integer> fakeControls = new HashSet<>(controls);
                int throttle = conductor.getThrottle();
                fakeControls.remove(0); fakeControls.remove(1);
                if (throttle > 0) fakeControls.add(0);
                else if (throttle < 0) fakeControls.add(1);
                if (reverseControlsPos != null && fakeControls.contains(1)) {
                    fakeControls.remove(1); fakeControls.add(0);
                    controlsPos = reverseControlsPos;
                }
                if (!fakeControls.isEmpty()) cce.control(controlsPos, fakeControls, conductor.fakePlayer);

                if (carriage != null) {
                    Train train = carriage.train;
                    if (isSprintKeyPressed && honkPacketCooldown-- <= 0) {
                        train.determineHonk(conductor.level());
                        if (train.lowHonk != null) {
                            Utils.sendHonkPacket(train, true);
                            honkPacketCooldown = 5;
                            usedToHonk = true;
                        }
                    }
                    if (!isSprintKeyPressed && usedToHonk) {
                        Utils.sendHonkPacket(train, false);
                        honkPacketCooldown = 0; usedToHonk = false;
                    }
                }
            } else if (targetDirection.lengthSqr() > 0.01) {
                conductor.getMoveControl().setWantedPosition(
                        target.x, getGroundY(target), target.z,
                        speedModifier * targetStrength / 15);
            }
        }

        private Set<Integer> getControls() {
            Set<Integer> c = new HashSet<>();
            if (conductor.forwardListener  != null && conductor.forwardListener.isPowered())  c.add(0);
            if (conductor.backwardListener != null && conductor.backwardListener.isPowered()) c.add(1);
            if (conductor.leftListener     != null && conductor.leftListener.isPowered())     c.add(2);
            if (conductor.rightListener    != null && conductor.rightListener.isPowered())    c.add(3);
            if (conductor.jumpListener     != null && conductor.jumpListener.isPowered())     c.add(4);
            if (conductor.sneakListener    != null && conductor.sneakListener.isPowered())    c.add(5);
            return c;
        }

        private Pair<Vec3, Double> calculateTarget() {
            double x = 0, y = 0, z = 0;
            if (conductor.forwardListener  != null && conductor.forwardListener.isPowered())  z -= conductor.forwardListener.receivedStrength;
            if (conductor.backwardListener != null && conductor.backwardListener.isPowered()) z += conductor.backwardListener.receivedStrength;
            if (conductor.leftListener     != null && conductor.leftListener.isPowered())     x -= conductor.leftListener.receivedStrength;
            if (conductor.rightListener    != null && conductor.rightListener.isPowered())    x += conductor.rightListener.receivedStrength;
            if (conductor.jumpListener     != null && conductor.jumpListener.isPowered())     y += conductor.jumpListener.receivedStrength;
            if (conductor.sneakListener    != null && conductor.sneakListener.isPowered())    y -= conductor.sneakListener.receivedStrength;
            int count = (x != 0 ? 1 : 0) + (y != 0 ? 1 : 0) + (z != 0 ? 1 : 0);
            double avg = count == 0 ? 0 : (Math.abs(x) + Math.abs(y) + Math.abs(z)) / count;
            return Pair.of(new Vec3(x, Math.max(0, y), z).scale(1 / 15.0), avg);
        }

        @Override
        public void start() {
            super.start();
            Pair<Vec3, Double> p = calculateTarget();
            targetDirection = p.getFirst(); targetStrength = p.getSecond();
        }

        @Override
        public void stop() { super.stop(); targetDirection = Vec3.ZERO; targetStrength = 15.0; }

        @Override
        public boolean requiresUpdateEveryTick() { return true; }
    }

    static class ConductorLookedAtGoal extends JobBasedGoal {
        @Nullable private LivingEntity target;

        public ConductorLookedAtGoal(ConductorEntity conductor) {
            super(conductor, Job.REDSTONE_OPERATOR);
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) return false;
            for (Player player : conductor.level().players()) {
                if (player.hasLineOfSight(conductor))
                    return conductor.distanceToSqr(player) < 256 && conductor.isLookingAtMe(player);
            }
            return false;
        }

        @Override
        public void start() {
            Level level = conductor.level();
            BlockPos pos = conductor.getEntityData().get(BLOCK);
            BlockState state = level.getBlockState(pos);
            ServerPlayer fake = conductor.fakePlayer;
            if (conductor.canReach(pos) && conductor.canUseBlock(state) && fake != null) {
                ClipContext ctx = new ClipContext(conductor.getEyePosition(),
                        new Vec3(pos.getX(), pos.getY(), pos.getZ()),
                        ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, fake);
                BlockHitResult hit = level.clip(ctx);
                if (!pos.equals(hit.getBlockPos())) return;
                boolean canUse = state.getShape(level, pos).isEmpty()
                        || EntityUtils.handleUseEvent(fake, InteractionHand.MAIN_HAND, hit);
                if (canUse) state.useWithoutItem(level, fake, hit);
            }
        }

        @Override
        public void tick() {
            if (target != null) conductor.lookControl.setLookAt(target);
        }
    }

    static class ConductorPonderBlockGoal extends JobBasedGoal {
        private BlockPos target;

        public ConductorPonderBlockGoal(ConductorEntity conductor) {
            super(conductor, Job.REDSTONE_OPERATOR);
            this.target = conductor.entityData.get(BLOCK);
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) return false;
            this.target = conductor.entityData.get(BLOCK);
            if (conductor.canReach(target) && conductor.canUseBlock(conductor.level().getBlockState(target)))
                return true;
            for (int y = -REACH.getY(); y < REACH.getY(); y++) {
                for (int x = -REACH.getX(); x < REACH.getX(); x++) {
                    for (int z = -REACH.getZ(); z < REACH.getZ(); z++) {
                        BlockPos at = conductor.blockPosition().offset(x, y, z);
                        BlockState state = conductor.level().getBlockState(at);
                        if (conductor.canUseBlock(state)) {
                            ClipContext ctx = new ClipContext(conductor.getEyePosition(),
                                    new Vec3(at.getX(), at.getY(), at.getZ()),
                                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, conductor);
                            if (conductor.level().clip(ctx).getBlockPos().equals(at)) {
                                this.target = at;
                                conductor.entityData.set(BLOCK, target);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void tick() {
            conductor.lookControl.setLookAt(target.getX(), target.getY(), target.getZ());
        }
    }
}
