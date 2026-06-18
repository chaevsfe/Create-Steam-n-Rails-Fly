/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.base.data;

import com.railwayteam.railways.registry.CRTags;
import com.railwayteam.railways.registry.CRTags.AllBlockTags;
import com.railwayteam.railways.registry.CRTags.AllItemTags;
import com.zurrtum.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on {@link TagGen}
 */
public class CRTagGen {
	private static final Map<TagKey<Block>, List<Identifier>> OPTIONAL_TAGS = new HashMap<>();

	@SafeVarargs
	public static void addOptionalTag(Identifier id, TagKey<Block>... tags) {
		for (TagKey<Block> tag : tags) {
			OPTIONAL_TAGS.computeIfAbsent(tag, (e) -> new ArrayList<>()).add(id);
		}
	}

	public static void generateBlockTags(RegistrateTagsProvider<Block> prov) {
		for (CRTags.AllBlockTags tag : CRTags.AllBlockTags.values()) {
			if (tag.alwaysDatagen) {
				tagAppender(prov, tag);
			}
		}
		for (TagKey<Block> tag : OPTIONAL_TAGS.keySet()) {
			var appender = tagAppender(prov, tag);
			for (Identifier loc : OPTIONAL_TAGS.get(tag))
				appender.addOptional(loc);
		}
	}

	public static void generateItemTags(RegistrateTagsProvider<Item> prov) {
		for (AllItemTags tag : AllItemTags.values()) {
			if (tag.alwaysDatagen)
				tagAppender(prov, tag);
		}
	}

	public static TagAppender<Item> tagAppender(RegistrateTagsProvider<Item> prov, AllItemTags tag) {
		return tagAppender(prov, tag.tag);
	}

	public static TagAppender<Block> tagAppender(RegistrateTagsProvider<Block> prov, AllBlockTags tag) {
		return tagAppender(prov, tag.tag);
	}

	public static <T> TagAppender<T> tagAppender(RegistrateTagsProvider<T> prov, TagKey<T> tag) {
		return new TagAppender<>();
	}

	public static class TagAppender<T> {
		public TagAppender<T> add(T entry) {
			return this;
		}

		@SafeVarargs
		public final TagAppender<T> add(T... entries) {
			return this;
		}

		public TagAppender<T> add(ResourceKey<T> entry) {
			return this;
		}

		public TagAppender<T> addOptional(Identifier id) {
			return this;
		}

		public TagAppender<T> addOptionalTag(Identifier id) {
			return this;
		}

		public TagAppender<T> forceAddTag(TagKey<T> tag) {
			return this;
		}
	}
}
