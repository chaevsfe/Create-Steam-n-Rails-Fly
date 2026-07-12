/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric;

import com.railwayteam.railways.ModSetup;
import com.zurrtum.create.api.registry.CreateRegisterPlugin;

/**
 * Bridges Railways registration into Create Fly's vanilla-bootstrap callbacks.
 *
 * <p>Minecraft 26.2 snapshots all block and fluid states at the end of the corresponding
 * vanilla class initializers. Create Fly exposes those exact points as the {@code create_plugin}
 * entrypoint; registering the states later from {@link RailwaysImpl#onInitialize()} leaves them
 * out of the vanilla id maps.</p>
 */
public final class RailwaysCreateRegisterPlugin implements CreateRegisterPlugin {
    private static boolean blocksRegistered;
    private static boolean fluidsRegistered;

    @Override
    public void onBlockRegister() {
        if (blocksRegistered)
            throw new IllegalStateException("Create Fly invoked Railways block registration more than once");

        ModSetup.registerBlocksEarly();
        blocksRegistered = true;
    }

    @Override
    public void onFluidRegister() {
        if (fluidsRegistered)
            throw new IllegalStateException("Create Fly invoked Railways fluid registration more than once");

        ModSetup.registerFluidsEarly();
        fluidsRegistered = true;
    }

    /**
     * Fails at the normal Fabric entrypoint when the Create Fly integration is absent or its
     * callback contract changed. Continuing would produce registered objects with incomplete
     * vanilla state-id maps and a much less actionable failure later in startup.
     */
    public static void verifyEarlyRegistrationComplete() {
        if (!blocksRegistered || !fluidsRegistered) {
            throw new IllegalStateException(
                "Create Fly did not invoke Railways early registration (blocks=" + blocksRegistered
                    + ", fluids=" + fluidsRegistered + ")"
            );
        }
    }
}
