package com.zurrtum.create.content.trains.track;

import com.railwayteam.railways.registry.CRTrackMaterials;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class TrackMaterialFactory {
    private final Identifier id;
    private Supplier<? extends TrackBlock> block;
    private TrackBlockFactory blockFactory = TrackBlock::new;
    private Identifier particle;
    private String langName;
    private Identifier trackType = CRTrackMaterials.CRTrackType.STANDARD;

    private TrackMaterialFactory(Identifier id) {
        this.id = id;
        this.particle = id.withPrefix("block/track/");
        this.langName = id.getPath();
    }

    public static TrackMaterialFactory make(Identifier id) {
        return new TrackMaterialFactory(id);
    }

    public TrackMaterialFactory block(Supplier<? extends Supplier<? extends TrackBlock>> block) {
        this.block = () -> block.get().get();
        return this;
    }

    public TrackMaterialFactory customBlockFactory(TrackBlockFactory blockFactory) {
        this.blockFactory = blockFactory;
        return this;
    }

    public TrackMaterialFactory customModels(Supplier<?> tie, Supplier<?> left, Supplier<?> right) {
        return this;
    }

    public TrackMaterialFactory lang(String langName) {
        this.langName = langName;
        return this;
    }

    public TrackMaterialFactory noRecipeGen() {
        return this;
    }

    public TrackMaterialFactory particle(Identifier particle) {
        this.particle = particle;
        return this;
    }

    public TrackMaterialFactory rails(ItemLike rails) {
        return this;
    }

    public TrackMaterialFactory sleeper(Block sleeper) {
        return this;
    }

    public TrackMaterialFactory sleeper(Item sleeper) {
        return this;
    }

    public TrackMaterialFactory sleeper(Object sleeper) {
        return this;
    }

    public TrackMaterialFactory standardModels() {
        return this;
    }

    public TrackMaterialFactory trackType(Identifier trackType) {
        this.trackType = trackType;
        return this;
    }

    public TrackMaterial build() {
        final TrackMaterial[] material = new TrackMaterial[1];
        Supplier<TrackBlock> blockSupplier = () -> block != null
            ? block.get()
            : blockFactory.create(BlockBehaviour.Properties.of(), material[0]);
        material[0] = new TrackMaterial(id, blockSupplier);
        CRTrackMaterials.registerMeta(material[0], trackType, particle, langName);
        return material[0];
    }

    public Identifier getId() {
        return id;
    }

    @FunctionalInterface
    public interface TrackBlockFactory {
        TrackBlock create(BlockBehaviour.Properties properties, TrackMaterial material);
    }
}
