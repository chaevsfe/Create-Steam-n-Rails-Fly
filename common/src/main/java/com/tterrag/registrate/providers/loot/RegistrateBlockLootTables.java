package com.tterrag.registrate.providers.loot;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

public class RegistrateBlockLootTables {
    public <T extends Block> void dropWhenSilkTouch(T block) {
    }

    public <T extends Block> void add(T block, LootTable.Builder table) {
    }

    public LootPool.Builder applyExplosionCondition(Block block, LootPool.Builder builder) {
        return builder;
    }

    public <T extends Block> void dropOther(T block, Block drop) {
    }

    public LootTable.Builder createDoorTable(Block block) {
        return LootTable.lootTable();
    }
}
