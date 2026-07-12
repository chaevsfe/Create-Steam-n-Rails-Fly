package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.util.packet.PacketSender;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class MountedToolbox extends ToolboxBlockEntity {
    private final ConductorEntity parent;

    // Create's ToolboxBlockEntity tracks connected players in a package-private field we can't reach
    // across the package boundary; mirror the connection here via the public connectPlayer hook so the
    // FollowToolboxPlayerGoal can find players linked to this toolbox.
    private static final int KEEP_ALIVE_TICKS = 100;
    private final Map<Player, Integer> trackedConnectedPlayers = new WeakHashMap<>();

    public MountedToolbox(ConductorEntity parent, DyeColor color) {
        super(parent.blockPosition(), AllBlocks.TOOLBOX.pick(color).defaultBlockState());
        this.parent = parent;
        setLevel(parent.level());
        setLazyTickRate(10);
    }

    public ConductorEntity getParent() {
        return parent;
    }

    @Override
    public void connectPlayer(int slot, Player player, int hotbarSlot) {
        super.connectPlayer(slot, player, hotbarSlot);
        trackedConnectedPlayers.put(player, KEEP_ALIVE_TICKS);
    }

    public List<Player> getConnectedPlayers() {
        return new ArrayList<>(trackedConnectedPlayers.keySet());
    }

    public void readFromItem(ItemStack stack) {
        readInventory(stack.get(AllDataComponents.TOOLBOX_INVENTORY));
        if (stack.has(AllDataComponents.TOOLBOX_UUID))
            setUniqueId(stack.get(AllDataComponents.TOOLBOX_UUID));
        if (stack.has(DataComponents.CUSTOM_NAME))
            setCustomName(stack.getHoverName());
    }

    @Override
    public void tick() {
        super.tick();
        if (!trackedConnectedPlayers.isEmpty()) {
            trackedConnectedPlayers.entrySet().removeIf(entry -> {
                int remaining = entry.getValue() - 1;
                entry.setValue(remaining);
                return remaining <= 0 || !entry.getKey().isAlive();
            });
        }
    }

    @Override
    protected void read(ValueInput input, boolean clientPacket) {
        super.read(input, clientPacket);
        input.getInt("Color").ifPresent(colorId -> {
            BlockState state = AllBlocks.TOOLBOX.pick(DyeColor.byId(colorId)).defaultBlockState();
            setBlockState(state);
        });
    }

    @Override
    protected void write(ValueOutput output, boolean clientPacket) {
        super.write(output, clientPacket);
        output.putInt("Color", getColor().getId());
    }

    public void read(CompoundTag tag, boolean clientPacket) {
        ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, parent.registryAccess(), tag);
        if (clientPacket)
            readClient(input);
        else
            loadWithComponents(input);
    }

    public CompoundTag write(boolean clientPacket) {
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, parent.registryAccess());
        if (clientPacket)
            writeClient(output);
        else
            saveAdditional(output);
        return output.buildResult();
    }

    public static MountedToolbox read(ConductorEntity parent, CompoundTag compound) {
        MountedToolbox toolbox = new MountedToolbox(parent, DyeColor.BROWN);
        toolbox.read(compound, false);
        return toolbox;
    }

    @Override
    public void sendData() {
        if (level == null || level.isClientSide())
            return;
        PacketSender.syncMountedToolboxNBT(parent, write(true));
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean canPlayerUse(Player player) {
        return player.distanceToSqr(parent) < 8 * 8;
    }

    public ItemStack getDisplayStack() {
        ItemStack stack = AllBlocks.TOOLBOX.pick(getColor()).asItem().getDefaultInstance();
        if (hasCustomName())
            stack.set(DataComponents.CUSTOM_NAME, getCustomName());
        return stack;
    }

    public ItemStack getCloneItemStack() {
        ItemStack stack = getDisplayStack();
        stack.set(AllDataComponents.TOOLBOX_INVENTORY, inventory);
        stack.set(AllDataComponents.TOOLBOX_UUID, getUniqueId());
        return stack;
    }

    @Override
    public MountedToolboxContainer createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
        sendToMenu(extraData);
        return new MountedToolboxContainer(id, inv, this);
    }

    @Override
    public void sendToMenu(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(parent.getId());
        buffer.writeNbt(write(true));
    }

    public static void openMenu(ServerPlayer player, MountedToolbox toolbox) {
        com.railwayteam.railways.content.conductor.toolbox.fabric.MountedToolboxImpl.openMenu(player, toolbox);
    }
}
