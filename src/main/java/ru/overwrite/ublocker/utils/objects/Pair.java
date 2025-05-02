package ru.overwrite.ublocker.utils.objects;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Pair<L, R> {

    private L left;
    private R right;

    public L left() {
        return this.left;
    }

    public R right() {
        return this.right;
    }

    public void left(final L l) {
        this.left = l;
    }

    public void right(final R r) {
        this.right = r;
    }

    public static <L, R> Pair<L, R> of(final L l, final R r) {
        return new Pair<>(l, r);
    }
}

