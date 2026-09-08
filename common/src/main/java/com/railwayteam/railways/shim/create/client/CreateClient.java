package com.railwayteam.railways.shim.create.client;

import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.GlobalRailwayManager;

public final class CreateClient {
    public static GlobalRailwayManager RAILWAYS() {
        return Create.RAILWAYS;
    }

    private CreateClient() {
    }
}
