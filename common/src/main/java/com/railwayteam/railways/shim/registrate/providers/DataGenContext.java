package com.railwayteam.railways.shim.registrate.providers;

import net.minecraft.resources.Identifier;

public record DataGenContext<R, T>(Identifier id, T entry) {
}
