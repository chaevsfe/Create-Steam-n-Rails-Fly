package com.railwayteam.railways.shim.registrate;

public abstract class AbstractRegistrate<T extends AbstractRegistrate<T>> {
    protected final String modid;

    protected AbstractRegistrate(String modid) {
        this.modid = modid;
    }

    public String getModid() {
        return modid;
    }
}
