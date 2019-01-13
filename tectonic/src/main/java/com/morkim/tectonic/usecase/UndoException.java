package com.morkim.tectonic.usecase;

import com.morkim.tectonic.flow.Step;

public class UndoException extends Exception {
    private Step step;

    public UndoException() {
        this.step = step;
    }

    public Step getStep() {
        return step;
    }
}
