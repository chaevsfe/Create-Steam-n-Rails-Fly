package com.railwayteam.railways.base.data.fabric;

import com.railwayteam.railways.base.data.BuilderTransformers;
import com.railwayteam.railways.content.buffer.BlockStateBlockItemGroup;
import com.railwayteam.railways.content.buffer.MonoTrackBufferBlock;
import com.railwayteam.railways.content.buffer.TrackBufferBlock;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBarsBlock;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.railwayteam.railways.content.buffer.headstock.HeadstockBlock;
import com.railwayteam.railways.content.buffer.single_deco.GenericDyeableSingleBufferBlock;
import com.railwayteam.railways.content.buffer.single_deco.LinkPinBlock;
import com.railwayteam.railways.content.conductor.vent.VentBlock;
import com.railwayteam.railways.content.conductor.whistle.ConductorWhistleFlagBlock;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlock;
import com.railwayteam.railways.content.custom_bogeys.blocks.base.CRBogeyBlock;
import com.railwayteam.railways.content.custom_bogeys.special.invisible.InvisibleBogeyBlock;
import com.railwayteam.railways.content.custom_bogeys.special.monobogey.InvisibleMonoBogeyBlock;
import com.railwayteam.railways.content.custom_bogeys.special.monobogey.MonoBogeyBlock;
import com.railwayteam.railways.content.custom_tracks.casing.CasingCollisionBlock;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlock;
import com.railwayteam.railways.content.handcar.HandcarBlock;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.RotatedPillarWindowBlock;
import com.railwayteam.railways.content.palettes.boiler.BoilerBlock;
import com.railwayteam.railways.content.palettes.hazard_stripes.HazardStripesBlock;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.railwayteam.railways.content.palettes.smokebox.PalettesSmokeboxBlock;
import com.railwayteam.railways.content.palettes.trapdoors.PalettesTrapDoorBlock;
import com.railwayteam.railways.content.semaphore.SemaphoreBlock;
import com.railwayteam.railways.content.smokestack.RotationType;
import com.railwayteam.railways.content.smokestack.block.SmokeStackBlock;
import com.railwayteam.railways.content.smokestack.block.diesel.DieselSmokeStackBlock;
import com.railwayteam.railways.content.smokestack.block.variable.VariableStack;
import com.railwayteam.railways.content.switches.TrackSwitchBlock;
import com.railwayteam.railways.registry.CRPalettes.WindowType;
import com.railwayteam.railways.registry.CRPalettes.Wrapping;
import com.railwayteam.railways.registry.CRTags;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.zurrtum.create.AllTags;
import com.zurrtum.create.content.decoration.MetalLadderBlock;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlock;
import com.zurrtum.create.foundation.data.SharedProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static com.zurrtum.create.foundation.data.TagGen.axeOrPickaxe;
import static com.zurrtum.create.foundation.data.TagGen.pickaxeOnly;

public class BuilderTransformersImpl {
    public static <B extends MonoBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> monobogey() {
        return b -> b.initialProperties(SharedProperties::softMetal)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK).noOcclusion())
            .transform(pickaxeOnly());
    }

    public static <B extends InvisibleBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> invisibleBogey() {
        return b -> b.initialProperties(SharedProperties::softMetal)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK).noOcclusion())
            .transform(pickaxeOnly());
    }

    public static <B extends InvisibleMonoBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> invisibleMonoBogey() {
        return b -> b.initialProperties(SharedProperties::softMetal)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK).noOcclusion())
            .transform(pickaxeOnly());
    }

    public static <B extends CRBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> standardBogey() {
        return b -> b.transform(BuilderTransformers.sharedBogey());
    }

    public static <B extends CRBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> wideBogey() {
        return b -> b.transform(BuilderTransformers.sharedBogey());
    }

    public static <B extends CRBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> narrowBogey() {
        return b -> b.transform(BuilderTransformers.sharedBogey());
    }

    // The following transforms only supplied generated blockstate/model data in the legacy port.
    // The 26.2 build consumes the checked-in generated resources directly.
    public static <B extends SemaphoreBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> semaphore() { return b -> b; }
    public static <B extends TrackCouplerBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> trackCoupler() { return b -> b; }
    public static <B extends TrackSwitchBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> trackSwitch(boolean andesite) { return b -> b; }
    public static <B extends ConductorWhistleFlagBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> conductorWhistleFlag() { return b -> b; }
    public static <B extends DieselSmokeStackBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> dieselSmokeStack() { return b -> b; }
    public static <B extends VentBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> conductorVent() { return b -> b; }
    public static NonNullBiConsumer<DataGenContext<Block, SmokeStackBlock>, RegistrateBlockstateProvider> defaultSmokeStack(String variant, RotationType rotType) { return (ctx, prov) -> {}; }
    public static <B extends Block & VariableStack> NonNullBiConsumer<DataGenContext<Block, B>, RegistrateBlockstateProvider> variableSmokeStack(String variant, RotationType rotType) { return (ctx, prov) -> {}; }
    public static <B extends CasingCollisionBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> casingCollision() { return b -> b; }

    public static <B extends HandcarBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> handcar() {
        return b -> b.initialProperties(SharedProperties::softMetal)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK).noOcclusion())
            .transform(pickaxeOnly());
    }

    public static <B extends GenericCrossingBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> genericCrossing() { return b -> b; }

    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> locoMetalBase(PalettesColor color, @Nullable String type) {
        return b -> b.initialProperties(SharedProperties::softMetal)
            .properties(p -> p
                .mapColor(color.getMapColor())
                .sound(SoundType.NETHERITE_BLOCK)
            )
            .transform(pickaxeOnly())
            .tag(AllTags.AllBlockTags.WRENCH_PICKUP.tag)
            .tag(CRTags.AllBlockTags.LOCOMETAL.tag);
    }

    public static <B extends RotatedPillarBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locoMetalPillar(PalettesColor color) {
        return b -> b.transform(locoMetalBase(color, null));
    }

    @SafeVarargs
    public static <B extends MetalLadderBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locoMetalLadder(PalettesColor color, String ladderType, TagKey<Item>... tags) {
        return b -> b.transform(locoMetalBase(color, null))
            .initialProperties(() -> Blocks.LADDER)
            .addLayer(() -> RenderType::cutout)
            // This port's lightweight BlockBuilder resets earlier property transforms when
            // initialProperties() is called, so restore the palette color after copying LADDER.
            .properties(p -> p.mapColor(color.getMapColor()).sound(SoundType.COPPER))
            .tag(BlockTags.CLIMBABLE)
            .item()
            .tag(tags)
            .build();
    }

    @SafeVarargs
    public static <B extends FlywheelBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locoMetalFlywheel(PalettesColor color, TagKey<Item>... tags) {
        return b -> b.transform(locoMetalBase(color, null))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(axeOrPickaxe())
            .item()
            .tag(tags)
            .build();
    }

    public static <B extends PalettesSmokeboxBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locoMetalSmokeBox(PalettesColor color, @Nullable Wrapping wrapping) {
        return b -> b.transform(locoMetalBase(color, null));
    }

    public static <B extends DoorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locometalHingedDoorBlockState(PalettesColor color, String type) { return b -> b; }
    public static <B extends DoorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locometalSlidingDoorBlockState(PalettesColor color, String type) { return b -> b; }
    public static <B extends DoorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locometalFoldingDoorBlockState(PalettesColor color, String type) { return b -> b; }

    public static <B extends RotatedPillarWindowBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locometalWindow(PalettesColor color, WindowType type) {
        return b -> b.transform(locoMetalBase(color, null));
    }

    public static <B extends PalettesTrapDoorBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locometalTrapdoor(PalettesColor color) {
        return b -> b.transform(locoMetalBase(color, null));
    }

    public static <B extends HazardStripesBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> hazardStripes(boolean chevron) { return b -> b; }
    public static <I extends BlockItem, P> NonNullUnaryOperator<ItemBuilder<I, P>> locometalDoorItemModel(PalettesColor color, String type) { return b -> b; }
    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> locoMetalItem(PalettesColor color) { return b -> b; }
    public static <I extends PaintPitcherItem, P> NonNullUnaryOperator<ItemBuilder<I, P>> paintPitcher() { return b -> b; }
    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> variantBuffer() { return b -> b; }
    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> variantBufferItem() { return b -> b; }
    public static <B extends CopycatHeadstockBarsBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> copycatHeadstockBars() { return b -> b; }
    public static <B extends TrackBufferBlock<?>, P> NonNullUnaryOperator<BlockBuilder<B, P>> bufferBlockState(Function<BlockState, Identifier> modelFunc, Function<BlockState, Direction> facingFunc) { return b -> b; }
    public static <B extends MonoTrackBufferBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> monoBuffer() { return b -> b; }
    public static <B extends LinkPinBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> linkAndPin() { return b -> b; }
    public static <B extends HeadstockBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> headstock() { return b -> b; }
    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> invisibleBlockState() { return b -> b; }

    public static <B extends CopycatHeadstockBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> copycatHeadstock() {
        return b -> b.properties(p -> p.noOcclusion().mapColor(MapColor.NONE));
    }

    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> copycatHeadstockItem() { return b -> b; }
    public static <B extends GenericDyeableSingleBufferBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> bigBuffer() { return b -> b; }
    public static <B extends GenericDyeableSingleBufferBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> smallBuffer() { return b -> b; }
}
