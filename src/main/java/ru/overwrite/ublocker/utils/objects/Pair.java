// Stolen from it.unimi.dsi.fastutil.Pair
package ru.overwrite.ublocker.utils.objects;

import java.util.Comparator;

public interface Pair<L, R> {

    L left();

    R right();

    default Pair<L, R> left(final L l) {
        throw new UnsupportedOperationException();
    }

    default Pair<L, R> right(final R r) {
        throw new UnsupportedOperationException();
    }

    default L first() {
        return left();
    }

    default R second() {
        return right();
    }

    default Pair<L, R> first(final L l) {
        return left(l);
    }

    default Pair<L, R> second(final R r) {
        return right(r);
    }

    default Pair<L, R> key(final L l) {
        return left(l);
    }

    default Pair<L, R> value(final R r) {
        return right(r);
    }

    default L key() {
        return left();
    }

    default R value() {
        return right();
    }

    static <L, R> Pair<L, R> of(final L l, final R r) {
        return new ImmutablePair<>(l, r);
    }

    @SuppressWarnings("unchecked")
    static <L, R> Comparator<it.unimi.dsi.fastutil.Pair<L, R>> lexComparator() {
        return (x, y) -> {
            final int t = ((Comparable<L>)x.left()).compareTo(y.left());
            if (t != 0) return t;
            return ((Comparable<R>)x.right()).compareTo(y.right());
        };
    }
}

