package io.leao.codecolors.core.color;

public class ReferencePair<U, V> {
    private Reference<U> firstRef;
    private Reference<V> secondRef;

    public ReferencePair() {
        this.firstRef = new Reference<>();
        this.secondRef = new Reference<>();
    }

    public Reference<U> getFirst() {
        return firstRef;
    }

    public Reference<V> getSecond() {
        return secondRef;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReferencePair<?, ?> that = (ReferencePair<?, ?>) o;

        if (!firstRef.equals(that.firstRef)) return false;
        return secondRef.equals(that.secondRef);
    }

    @Override
    public int hashCode() {
        int result = firstRef.hashCode();
        result = 31 * result + secondRef.hashCode();
        return result;
    }
}