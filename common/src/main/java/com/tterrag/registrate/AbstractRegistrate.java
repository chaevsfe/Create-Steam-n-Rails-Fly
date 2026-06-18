package com.tterrag.registrate;

public abstract class AbstractRegistrate<T extends AbstractRegistrate<T>> {
    protected final String modid;

    protected AbstractRegistrate(String modid) {
        this.modid = modid;
    }

    public String getModid() {
        return modid;
    }
}
