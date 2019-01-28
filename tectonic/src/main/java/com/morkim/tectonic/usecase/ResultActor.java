package com.morkim.tectonic.usecase;

/**
 * The result actor to observe the use case instead of being a direct actor defined by it. This actor
 * can observe that use case is either completed or aborted. The result actor can configured for a
 * use case by either calling {@link UseCase#addResultActor(ResultActor)} or through the builder method
 * {@link Builder#resultActor(ResultActor[])}. A use case does not return a result, but a we need to
 * add a feature where we can make a use define a result that can be returned through some template.
 *
 * @param <E> the event type used by the system
 * @param <R> the result defined by the use case (currently not supported)
 */
public interface ResultActor<E, R> {
    
    void onComplete(E event, R result);
    
    void onAbort(E event);
}
