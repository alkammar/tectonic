package com.morkim.tectonic.simplified;

import com.morkim.tectonic.flow.Step;

public interface PrimaryActor<R> {

    void onStart(UseCaseHandle handle);

    void onComplete(R result);

    void onUndo(Step step);

    void onAbort();
}
