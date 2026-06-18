package net.minecraft.resources;

public class ResourceLocation {
    private final String namespace;
    private final String path;

    public ResourceLocation(String namespace, String path) {
        this.namespace = namespace;
        this.path = path;
    }

    public ResourceLocation(String value) {
        Identifier id = Identifier.parse(value);
        this.namespace = id.getNamespace();
        this.path = id.getPath();
    }

    public String getNamespace() {
        return namespace;
    }

    public String getPath() {
        return path;
    }

    public Identifier toIdentifier() {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
    public String toString() {
        return namespace + ":" + path;
    }
}
