package com.morkim.tectonic.usecase;

import java.util.Set;
import java.util.UUID;

/**
 * Implement this interface to receive the application events in order to trigger the corresponding
 * use cases. This interface is also used by the use case internally to execute precondition use cases.
 * If a precondition or sub is executed it will call the {@link #map(Class, TectonicEvent)} method first before a call
 * is made to any version of {@link #trigger(TectonicEvent, PreconditionActor, ResultActor, TectonicEvent)}.
 * So make sure that you do the all use cases executions inside {@link #trigger(TectonicEvent, PreconditionActor, ResultActor, TectonicEvent)}
 * and the other versions of it just calling it providing null values to the parameters that it does not have.
 *
 * @param <E> application event type
 *
 * @see UseCase#execute(TectonicEvent) and its versions
 * @see UseCase#execute(UUID, Class) and its versions
 * @see UseCase#onAddPreconditions(Set)
 */
public interface Triggers<E extends TectonicEvent> {

    /**
     * Make sure that other version of this method will call it rather than executing them selves.
     * Implement this by building ({@link Builder}) or fetching ({@link UseCase#fetch(Class)}) and
     * executing a use case corresponding to the passed {@code event}, specifying the
     * {@code preconditionActor} (if any), the {@code the resultActor} (if any) and the {@code} (if any).
     * You should handle any null parameter as they are all allowed.
     * The precondition actor here is the use case that is waiting for this use case to finish as a
     * precondition for it to start execution defined by {@link UseCase#onAddPreconditions(Set)}
     * The result actor can be either the one that executed the use case, like another use case or any
     * other parts of the application. It can also be one of its defined actors. It can also be none
     * of the previous, there are no restrictions on who can be interested in the use case result.
     *
     * @param event the event triggered the use case that is going to be executed
     * @param preconditionActor the precondition actor if this use case executed as a precondition
     *                          to another use case
     * @param resultActor the result actor waiting for the outcome of the use case.
     * @param contextEvent the context event this use case is executing within
     * @return the event triggered the use case
     *
     * @see UseCase#onAddPreconditions(Set)
     * @see ResultActor
     */
    E trigger(E event, PreconditionActor preconditionActor, ResultActor resultActor, E contextEvent);

    /**
     * Version of {@link #trigger(TectonicEvent, PreconditionActor, ResultActor, TectonicEvent)} but
     * without {@code preconditionActor} or {@code contextEvent}
     */
    E trigger(E event, ResultActor resultActor);

    /**
     * Version of {@link #trigger(TectonicEvent, PreconditionActor, ResultActor, TectonicEvent)} but
     * without {@code preconditionActor, resultActor} or {@code contextEvent}
     */
    E trigger(E event);

    /**
     * Maps the use case {@code cls} to be executed to a system trigger event {@code E}, passing a
     * {@code contextEvent} indicating in which use case context event this use case is going to execute.
     * This method is called internally by the use case when it requires executing a sub use case.
     * This is followed by a call to {@link #trigger(TectonicEvent, PreconditionActor, ResultActor, TectonicEvent)},
     * which receives the event returned in this mapping.
     * For example use case A triggered by event E1, is executing use case B as a sub use case.
     * Here E1 is the {@code contextEvent}. The event returned is the one that will be associated with
     * the execution of B.
     *
     * @param cls the use case class
     * @param contextEvent context event of the triggering use case
     * @return the event to use to trigger the use case {@code cls}
     *
     * @see UseCase#execute(Class)
     * @see UseCase#execute(UUID, Class)
     */
    E map(Class<? extends UseCase> cls, E contextEvent);

    /**
     * Allows to observe the execution of implicit (sub and precondition) use case, giving the system
     * the ability to create, set and clear dependencies. If using dependency injection create the dependencies
     * here. Return a result actor to know when the use case will finish so dependencies can be cleared.
     * If not using dependency injection set the dependencies using the {@code useCase} instance.
     *
     * @param contextEvent the context event of the container use case use case
     * @param implicitEvent the event triggered the implicit use case
     * @param useCase the use case instance
     * @return a result actor to observe the use case state
     */
    ResultActor<E, ?> observe(E contextEvent, E implicitEvent, UseCase<?> useCase);
}
