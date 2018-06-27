package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

public interface UseCaseHandle {

    void undo(Step step, int... actions);

    void abort();
}
