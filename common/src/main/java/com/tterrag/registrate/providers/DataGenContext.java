package com.tterrag.registrate.providers;

import net.minecraft.resources.Identifier;

public record DataGenContext<R, T>(Identifier id, T entry) {
}
