package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

import java.util.UUID;

public interface SecondaryHandle {

    void undo(Step step, UUID... actions);
}
