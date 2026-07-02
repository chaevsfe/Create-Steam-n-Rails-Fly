package com.zurrtum.create;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class AllTags {
    public enum AllBlockTags {
        COPYCAT_DENY("copycat_deny"),
        GIRDABLE_TRACKS("girdable_tracks"),
        MOVABLE_EMPTY_COLLIDER("movable_empty_collider"),
        NON_DOUBLE_DOOR("non_double_door"),
        SAFE_NBT("safe_nbt"),
        TRACKS("tracks"),
        WRENCH_PICKUP("wrench_pickup");

        public final TagKey<Block> tag;

        AllBlockTags(String path) {
            tag = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("create", path));
        }

        public boolean matches(Block block) {
            return block.defaultBlockState().is(tag);
        }

        public boolean matches(BlockState state) {
            return state.is(tag);
        }
    }

    public enum AllItemTags {
        CONTRAPTION_CONTROLLED("contraption_controlled"),
        SLEEPERS("sleepers"),
        TOOLBOXES("toolboxes"),
        TRACK_NUGGETS("track_nuggets"),
        TRACKS("tracks"),
        UPRIGHT_ON_BELT("upright_on_belt"),
        WRENCH("wrench");

        public final TagKey<Item> tag;

        AllItemTags(String path) {
            tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("create", path));
        }

        public boolean matches(Item item) {
            return item.getDefaultInstance().is(tag);
        }

        public boolean matches(ItemStack stack) {
            return stack.is(tag);
        }
    }
}
