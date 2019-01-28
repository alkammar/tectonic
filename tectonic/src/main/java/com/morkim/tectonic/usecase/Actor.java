package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

import java.util.UUID;

/**
 * The actor interface is the base for all use case actors. Although for some actors you won't need
 * to treat them as typical actors (i.e receiving the {@code onStart}), {@code onUndo}), {@code onComplete})
 * or the {@code onAbort}) callbacks, yet it is recommended that all your actors implement either this
 * interface or any of its descendants
 *
 * @param <E> the event type used by the system
 */
public interface Actor<E> {

    /**
     * Called to notify the start of the execution of the use case.
     *
     * @param event the event that triggered the use case
     * @param handle a handle to use to abort the use case or undo a step in the use case
     */
    void onStart(E event, UseCaseHandle handle);

    /**
     * Callback to notify that use case has undone the {@code step}. A single step can consist of one or
     * more cached pieces of data. The callback is trigger by either call to {@link UseCaseHandle#undo()}
     * or throwing {@link UndoException} in a blocking method (e.g. {@link UseCaseHandle#waitFor(Actor, UUID)}
     * and its overloads)
     *
     * @param step the step undone
     * @param inclusive if this step is included in the undo
     */
    void onUndo(Step step, boolean inclusive);

    /**
     * Callback to notify that the use case has completed successfully. This is either triggered by
     * a call to {@link UseCase#complete()} method or the use case is completed by a use case passed
     * to one of {@link UseCase#completeWhenCompleted(UseCase)} or {@link UseCase#completeWhenAborted(UseCase)}
     *
     * @param event the event that triggered the use case
     */
    void onComplete(E event);

    /**
     * Callback to notify that the use case was aborted successfully. This is either triggered by
     * a call to {@link UseCase#abort()} method or the use case is aborted by a use case passed
     * to one of {@link UseCase#abortWhenCompleted(UseCase)} or {@link UseCase#abortWhenAborted(UseCase)}
     *
     * @param event the event that triggered the use case
     */
    void onAbort(E event);
}
