package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

public interface Actor<E, R> extends ResultActor<E, R> {

    /**
     * Called to notify the start of the execution of the use case body
     * @param event the event that triggered the use case
     * @param handle a handle to use to abort the use case or undo a step in the use case
     */
    void onStart(E event, UseCaseHandle handle);

    /**
     * Callback to notify that use case has undone the {@code step}. A single step can consist of one or
     * more cached pieces of data
     * @param step the step undone
     */
    void onUndo(Step step);
}
