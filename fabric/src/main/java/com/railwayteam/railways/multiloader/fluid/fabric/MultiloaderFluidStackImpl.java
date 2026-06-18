package com.railwayteam.railways.multiloader.fluid.fabric;

import com.mojang.serialization.Codec;
import com.railwayteam.railways.multiloader.fluid.MultiloaderFluidStack;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiloaderFluidStackImpl extends MultiloaderFluidStack {
    private Fluid fluid;
    private long amount;
    private CompoundTag tag;

    public MultiloaderFluidStackImpl(Fluid fluid, long amount, @Nullable CompoundTag tag) {
        this.fluid = fluid;
        this.amount = amount;
        this.tag = tag;
    }

    private static Codec<MultiloaderFluidStack> makeCodec() {
        return Codec.STRING.xmap(ignored -> makeEmpty(), ignored -> "");
    }

    private static MultiloaderFluidStack makeEmpty() {
        return new MultiloaderFluidStackImpl(Fluids.EMPTY, 0, null);
    }

    public static MultiloaderFluidStack create(Fluid fluid, long amount, @Nullable CompoundTag nbt) {
        return new MultiloaderFluidStackImpl(fluid, amount, nbt);
    }

    public static MultiloaderFluidStack loadFluidStackFromNBT(CompoundTag tag) {
        return makeEmpty();
    }

    public static MultiloaderFluidStack readFromPacket(FriendlyByteBuf buffer) {
        return makeEmpty();
    }

    @Override
    public MultiloaderFluidStack setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public Fluid getFluid() { return fluid; }

    @Override
    public long getAmount() { return amount; }

    @Override
    public boolean isEmpty() { return fluid == Fluids.EMPTY || amount <= 0; }

    @Override
    public boolean isFluidEqual(MultiloaderFluidStack other) { return other != null && other.getFluid() == fluid; }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) { return nbt; }

    @Override
    public void setTag(CompoundTag tag) { this.tag = tag; }

    @Nullable
    @Override
    public CompoundTag getTag() { return tag; }

    @Override
    public Component getDisplayName() { return Component.empty(); }

    @Override
    public FriendlyByteBuf writeToPacket(FriendlyByteBuf buffer) { return buffer; }

    @Override
    public MultiloaderFluidStack copy() { return new MultiloaderFluidStackImpl(fluid, amount, tag == null ? null : tag.copy()); }

    @Override
    public boolean containsFluid(@NotNull MultiloaderFluidStack other) { return isFluidEqual(other) && amount >= other.getAmount(); }

    @Override
    public boolean isFluidStackIdentical(MultiloaderFluidStack other) { return containsFluid(other) && amount == other.getAmount(); }

    @Override
    public boolean isFluidEqual(@NotNull ItemStack other) { return false; }

    @Override
    public boolean isLighterThanAir() { return false; }

    @Override
    public FluidIngredient asFluidIngredient() { return null; }
}
