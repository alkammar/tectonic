package com.morkim.tectonic.simplified;

import com.morkim.tectonic.flow.Step;

public interface UseCaseHandle {

    void undo(Step step, int... actions);

    void abort();
}
