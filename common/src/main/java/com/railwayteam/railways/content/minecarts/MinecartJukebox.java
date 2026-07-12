package com.railwayteam.railways.content.minecarts;

import com.railwayteam.railways.registry.CREntities;
import com.railwayteam.railways.registry.CRItems;
import com.railwayteam.railways.util.packet.PacketSender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.phys.Vec3;

public class MinecartJukebox extends MinecartBlock {
    private static final int COOLDOWN = 100;

    private int cooldownCount = 0;
    private ItemStack disc = ItemStack.EMPTY;
    @Environment(EnvType.CLIENT)
    private JukeboxCartSoundInstance sound;

    public MinecartJukebox(EntityType<?> type, Level level) {
        super(type, level, Blocks.JUKEBOX);
    }

    public MinecartJukebox(Level level, double x, double y, double z) {
        super(CREntities.CART_JUKEBOX.get(), level, x, y, z, Blocks.JUKEBOX);
    }

    public int getComparatorOutput() {
        return JukeboxSong.fromStack(disc)
            .map(Holder::value)
            .map(JukeboxSong::comparatorOutput)
            .orElse(0);
    }

    @Override
    public void tick() {
        super.tick();
        if (cooldownCount > 0)
            cooldownCount--;
    }

    @Override
    public void activateMinecart(ServerLevel level, int x, int y, int z, boolean active) {
        if (active && cooldownCount <= 0) {
            cooldownCount = COOLDOWN;
            PacketSender.updateJukeboxClientside(this, this.disc);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setDisc(input.read("Disc", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("Disc", ItemStack.OPTIONAL_CODEC, disc);
    }

    @Override
    public ItemStack getPickResult() {
        return CRItems.ITEM_JUKEBOXCART.asStack();
    }

    @NotNull
    @Override
    public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand, @NotNull Vec3 location) {
        InteractionResult ret = super.interact(player, hand, location);
        if (ret.consumesAction())
            return ret;

        Level level = level();
        if (!level.isClientSide()) {
            if (disc.isEmpty()) {
                ItemStack handStack = player.getItemInHand(hand);
                if (!isMusicDisc(handStack))
                    return InteractionResult.PASS;

                insertRecord(handStack);
                if (!player.isCreative())
                    player.setItemInHand(hand, ItemStack.EMPTY);
                player.awardStat(Stats.PLAY_RECORD);
            } else {
                ejectRecord((ServerLevel) level);
            }
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    public void insertRecord(ItemStack record) {
        setDisc(record);
        if (!level().isClientSide())
            PacketSender.updateJukeboxClientside(this, this.disc);
        else
            updateClientSound();
    }

    public ItemStack getDisc() {
        return disc;
    }

    private void setDisc(ItemStack record) {
        disc = record.copy();
        content = content.setValue(JukeboxBlock.HAS_RECORD, !disc.isEmpty());
    }

    private boolean isMusicDisc(ItemStack stack) {
        return !stack.isEmpty() && stack.has(DataComponents.JUKEBOX_PLAYABLE);
    }

    private void ejectRecord(ServerLevel level) {
        ItemStack outStack = disc.copy();
        ItemEntity out = new ItemEntity(level, getX() + 0.5d, getY() + 1d, getZ() + 0.5d, outStack);
        out.setDefaultPickUpDelay();
        level.addFreshEntity(out);
        insertRecord(ItemStack.EMPTY);
    }

    @Environment(EnvType.CLIENT)
    private void updateClientSound() {
        if (disc.isEmpty()) {
            if (sound != null)
                sound.requestStop();
            return;
        }

        if (sound != null && !sound.isStopped())
            sound.requestStop();

        JukeboxSong.fromStack(disc)
            .map(Holder::value)
            .ifPresent(this::startPlaying);
    }

    @Environment(EnvType.CLIENT)
    private void startPlaying(JukeboxSong song) {
        sound = new JukeboxCartSoundInstance(song);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    @Environment(EnvType.CLIENT)
    public class JukeboxCartSoundInstance extends AbstractTickableSoundInstance {
        public JukeboxCartSoundInstance(JukeboxSong song) {
            super(song.soundEvent().value(), SoundSource.RECORDS, SoundInstance.createUnseededRandom());
            this.x = MinecartJukebox.this.getX();
            this.y = MinecartJukebox.this.getY();
            this.z = MinecartJukebox.this.getZ();
        }

        @Override
        public void tick() {
            if (MinecartJukebox.this.isRemoved())
                requestStop();

            this.x = MinecartJukebox.this.getX();
            this.y = MinecartJukebox.this.getY();
            this.z = MinecartJukebox.this.getZ();
        }

        public void requestStop() {
            stop();
        }
    }

    @Override
    protected void destroy(ServerLevel level, DamageSource source) {
        ItemStack droppedDisc = disc.copy();
        super.destroy(level, source);
        if (!source.is(DamageTypeTags.IS_EXPLOSION)
            && level.getGameRules().get(GameRules.ENTITY_DROPS)
            && !droppedDisc.isEmpty()) {
            spawnAtLocation(level, droppedDisc);
            disc = ItemStack.EMPTY;
        }
    }
}
