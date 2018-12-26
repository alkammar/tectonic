package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

public interface SecondaryActor<E, R> {

    void onStart(E event, SecondaryHandle handle);

    void onUndo(Step step);

    void onFinish(E event, R result);
}
