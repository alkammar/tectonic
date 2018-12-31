package com.morkim.tectonic.usecase;

/**
 * The primary actor for a use case callbacks. There should be only one primary actor per use case
 * @param <E> the event type of the use case
 * @param <R>
 */
public interface PrimaryActor<E, R> extends Actor<E, R> {

}
