package com.morkim.tectonic.usecase;

public class Random<T> {

    private T value;
    private boolean set;

    public Random() {

    }

    public Random(T value) {
        this.value = value;
        set = true;
    }

    public T value(T value) {
        this.value = value;
        set = true;
        return value;
    }

    public T value() {
        return value;
    }

    public boolean isSet() {
        return set;
    }

    @Override
    public String toString() {
        if (value != null) return value.toString();
        return super.toString();
    }
}
