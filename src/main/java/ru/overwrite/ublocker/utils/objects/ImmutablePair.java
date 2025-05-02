// Stolen from it.unimi.dsi.fastutil.ImmutablePair and modified
package ru.overwrite.ublocker.utils.objects;

import java.io.Serial;
import java.io.Serializable;

public record ImmutablePair<K, V>(K left, V right) implements Pair<K, V>, Serializable {

    @Serial
    private static final long serialVersionUID = 0L;

    public static <K, V> ImmutablePair<K, V> of(final K left, final V right) {
        return new ImmutablePair<K, V>(left, right);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(Object other) {
        if (other == null)
            return false;
        if (other instanceof it.unimi.dsi.fastutil.Pair) {
            return java.util.Objects.equals((left), ((it.unimi.dsi.fastutil.Pair) other).left())
                    && java.util.Objects.equals((right), ((it.unimi.dsi.fastutil.Pair) other).right());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ((left) == null ? 0 : (left).hashCode()) * 19 + ((right) == null ? 0 : (right).hashCode());
    }

    @Override
    public String toString() {
        return "<" + left() + "," + right() + ">";
    }
}
