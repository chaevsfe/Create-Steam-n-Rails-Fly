package com.railwayteam.railways.content.palettes.boiler.fabric;

import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.boiler.BoilerGenerator;
import com.railwayteam.railways.registry.CRPalettes.Wrapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoilerGeneratorImpl extends BoilerGenerator {
    protected BoilerGeneratorImpl(@NotNull PalettesColor color, @Nullable Wrapping wrapping) {
        super(color, wrapping);
    }

    public static BoilerGenerator create(@NotNull PalettesColor color, @Nullable Wrapping wrapping) {
        return new BoilerGeneratorImpl(color, wrapping);
    }
}
