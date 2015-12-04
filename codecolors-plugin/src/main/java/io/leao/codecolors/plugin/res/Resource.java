package io.leao.codecolors.plugin.res;

public class Resource {
    public String name;
    public Type type;

    public Resource(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Resource resource = (Resource) o;

        if (name != null ? !name.equals(resource.name) : resource.name != null) return false;
        return type == resource.type;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    public enum Type {
        DRAWABLE, COLOR, ATTR, ANDROID_ATTR
    }
}
