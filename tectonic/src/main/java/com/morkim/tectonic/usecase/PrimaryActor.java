package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

public interface PrimaryActor<E, R> extends ResultActor<E, R> {

    void onStart(UseCaseHandle handle);

    void onUndo(Step step);
}
