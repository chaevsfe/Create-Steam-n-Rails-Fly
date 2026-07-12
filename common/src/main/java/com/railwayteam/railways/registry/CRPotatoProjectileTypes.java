package com.railwayteam.railways.registry;

import com.mojang.serialization.MapCodec;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.painting.PaintPitcherItem;
import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;

public class CRPotatoProjectileTypes {
    public enum PaintAction implements PotatoProjectileBlockHitAction {
        INSTANCE;

        public static final MapCodec<PaintAction> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public boolean execute(LevelAccessor level, ItemStack projectile, BlockHitResult hit) {
            if (!(projectile.getItem() instanceof PaintPitcherItem item)) return false;

            item.projectilePaint(projectile, level, hit);
            return true;
        }

        @Override
        public MapCodec<? extends PotatoProjectileBlockHitAction> codec() {
            return CODEC;
        }
    }

    public static void register() {
        Registry.register(
            CreateRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION,
            Railways.asResource("paint"),
            PaintAction.CODEC
        );
    }
}
