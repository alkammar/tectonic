package com.morkim.tectonic.usecase;

public class RandomAction<D> extends Action<D> {

    private int key;
    private D defaultValue;

    RandomAction(int key, D defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public void reset() {
        set(defaultValue);
    }

    public int getKey() {
        return key;
    }
}
