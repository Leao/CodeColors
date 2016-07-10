package io.leao.codecolors.core.color;

import java.lang.ref.WeakReference;

public class Reference<T> {
    /*
     * if objectRef != null ? hashCode != null && object == null
     * if object != null ? objectRef == null && hashCode == null
     */
    private WeakReference<T> objectRef;
    private Integer hashCode;
    private T object;

    public T get() {
        if (objectRef != null) {
            return objectRef.get();
        } else {
            return object;
        }
    }

    public void setWeak(T object) {
        objectRef = new WeakReference<>(object);
        hashCode = object != null ? object.hashCode() : 0;
        this.object = null;
    }

    public void set(T object) {
        objectRef = null;
        hashCode = null;
        this.object = object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Object object = get();

        Reference that = (Reference) o;
        Object thatObject = that.get();

        return object != null ? object.equals(thatObject) : thatObject == null;
    }

    @Override
    public int hashCode() {
        return object != null ? object.hashCode() : hashCode;
    }
}