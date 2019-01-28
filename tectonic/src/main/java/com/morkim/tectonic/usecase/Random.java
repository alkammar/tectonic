package com.morkim.tectonic.usecase;

import java.util.UUID;

/**
 * This is use signal that the data the use case is waiting for can come in any order. This is usually
 * use when you have multiple of data pieces the use case is gathering from an actor and the actor can
 * provide them in any order, which is typical for UI actors.
 * In the actor, typically it call(s) to {@link UseCaseHandle#waitForRandom(UUID)} when the data is
 * requested by the use case and then a series of calls to {@link UseCaseHandle#replyWith(UUID)} or
 * its overload and then a call to {@link UseCaseHandle#replyWithRandom(UUID)} or its overload.
 *
 * @param <T> the data type
 */
public class Random<T> {

    private T value;
    private boolean set;

    /**
     * This means that the data value is not set yet.
     */
    public Random() {

    }

    /**
     * Constructor with value. This means that the data is initialized even if null is passed.
     *
     * @param value the data value
     */
    public Random(T value) {
        this.value = value;
        set = true;
    }

    /**
     * Sets the data value.
     *
     * @param value the data value
     * @return the value set
     */
    public T value(T value) {
        this.value = value;
        set = true;
        return value;
    }

    /**
     * Returns the data value
     *
     * @return the value
     */
    public T value() {
        return value;
    }

    /**
     * Checks whether a value is set or not for the this piece of data
     *
     * @return true of set
     */
    public boolean isSet() {
        return set;
    }

    @Override
    public String toString() {
        if (value != null) return value.toString();
        return super.toString();
    }
}
