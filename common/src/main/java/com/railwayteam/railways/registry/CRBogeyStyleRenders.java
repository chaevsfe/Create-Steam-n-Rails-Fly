package com.railwayteam.railways.registry;

import com.railwayteam.railways.content.custom_bogeys.renderer.narrow.NarrowDoubleScotchYokeBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.narrow.NarrowScotchYokeBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.narrow.NarrowSmallBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.HandcarBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.double_axle.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.large.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.medium.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.single_axle.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.standard.triple_axle.*;
import com.railwayteam.railways.content.custom_bogeys.renderer.unified.BogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.wide.WideComicallyLargeScotchYokeBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.wide.WideDefaultBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.renderer.wide.WideScotchYokeBogeyDisplay;
import com.railwayteam.railways.content.custom_bogeys.special.invisible.InvisibleBogeyRenderer;
import com.railwayteam.railways.content.custom_bogeys.special.invisible.InvisibleBogeyVisual;
import com.railwayteam.railways.content.custom_bogeys.special.monobogey.MonoBogeyDisplay;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.content.trains.bogey.SizeRenderer;

import java.util.function.Supplier;

public final class CRBogeyStyleRenders {
    private CRBogeyStyleRenders() {}

    public static void register() {
        AllBogeyStyleRenders.register(CRBogeyStyles.MONOBOGEY, factory(MonoBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.INVISIBLE, CRBogeyStyleRenders::invisible);
        AllBogeyStyleRenders.register(CRBogeyStyles.INVISIBLE_MONOBOGEY, CRBogeyStyleRenders::invisible);

        AllBogeyStyleRenders.register(CRBogeyStyles.SINGLEAXLE, simple(SingleaxleBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.LEAFSPRING, simple(LeafspringBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.COILSPRING, simple(CoilspringBogeyDisplay::new));

        AllBogeyStyleRenders.register(CRBogeyStyles.FREIGHT, simple(FreightBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.ARCHBAR, simple(ArchbarBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.PASSENGER, simple(PassengerBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MODERN, simple(ModernBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.BLOMBERG, simple(BlombergBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.Y25, simple(Y25BogeyDisplay::new));

        AllBogeyStyleRenders.register(CRBogeyStyles.HEAVYWEIGHT, simple(HeavyweightBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.RADIAL, simple(RadialBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.HANDCAR, simple(HandcarBogeyDisplay::new));

        AllBogeyStyleRenders.register(CRBogeyStyles.WIDE_DEFAULT,
            simple(WideDefaultBogeyDisplay::new), simple(WideScotchYokeBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.WIDE_COMICALLY_LARGE,
            simple(WideComicallyLargeScotchYokeBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.NARROW_DEFAULT,
            simple(NarrowSmallBogeyDisplay::new), simple(NarrowScotchYokeBogeyDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.NARROW_DOUBLE_SCOTCH,
            simple(NarrowDoubleScotchYokeBogeyDisplay::new));

        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_STANDARD, simple(MediumStandardDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_SINGLE_WHEEL, simple(MediumSingleWheelDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_2_0_2_TRAILING, simple(Medium202TrailingDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_4_0_4_TRAILING, simple(Medium404TrailingDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_TRIPLE_WHEEL, simple(MediumTripleWheelDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_6_0_6_TRAILING, simple(Medium606TrailingDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_6_0_6_TENDER, simple(Medium606TenderDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_QUADRUPLE_WHEEL, simple(MediumQuadrupleWheelDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_8_0_8_TENDER, simple(Medium808TenderDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_QUINTUPLE_WHEEL, simple(MediumQuintupleWheelDisplay::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.MEDIUM_10_0_10_TENDER, simple(Medium10010TenderDisplay::new));

        AllBogeyStyleRenders.register(CRBogeyStyles.LARGE_CREATE_STYLED_0_4_0, simple(LargeCreateStyled040Display::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.LARGE_CREATE_STYLED_0_6_0, simple(LargeCreateStyled060Display::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.LARGE_CREATE_STYLED_0_8_0, simple(LargeCreateStyled080Display::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.LARGE_CREATE_STYLED_0_10_0, simple(LargeCreateStyled0100Display::new));
        AllBogeyStyleRenders.register(CRBogeyStyles.LARGE_CREATE_STYLED_0_12_0, simple(LargeCreateStyled0120Display::new));
    }

    private static Supplier<SizeRenderer> simple(BogeyDisplay.SimpleFactory factory) {
        return () -> BogeyDisplay.createSizeRenderer(factory);
    }

    private static Supplier<SizeRenderer> factory(BogeyDisplay.Factory factory) {
        return () -> BogeyDisplay.createSizeRenderer(factory);
    }

    private static SizeRenderer invisible() {
        return new SizeRenderer(InvisibleBogeyRenderer::new, InvisibleBogeyVisual::new);
    }
}
