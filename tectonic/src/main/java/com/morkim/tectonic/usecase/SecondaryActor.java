package com.morkim.tectonic.usecase;

/**
 * The secondary actor for a use case. A secondary actor is similar a {@link PrimaryActor} except that
 * it will not be able to abort a use case and its steps will be undone until the top primary step is
 * reached.
 *
 * @param <E> the event type used by the system
 */
public interface SecondaryActor<E> extends Actor<E> {

}
