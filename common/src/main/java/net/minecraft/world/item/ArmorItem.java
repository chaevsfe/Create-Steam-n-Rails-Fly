package net.minecraft.world.item;

public class ArmorItem extends Item {
    public enum Type {
        HELMET
    }

    protected final ArmorMaterial material;
    protected final Type type;

    public ArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(properties);
        this.material = material;
        this.type = type;
    }
}
