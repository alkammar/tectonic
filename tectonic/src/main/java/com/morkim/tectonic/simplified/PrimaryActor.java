package com.morkim.tectonic.simplified;

import com.morkim.tectonic.Precondition;
import com.morkim.tectonic.flow.Step;

public interface PrimaryActor<E, R> {

    void onStart(UseCaseHandle handle);

    void onComplete(E event, R result);

    void onUndo(Step step);

    void onAbort(E event);
}
