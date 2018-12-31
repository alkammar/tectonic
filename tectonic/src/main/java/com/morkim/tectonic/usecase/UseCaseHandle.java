package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

import java.util.UUID;

public interface UseCaseHandle {

    void undo(Step step, UUID... actions);

    void abort();
}
