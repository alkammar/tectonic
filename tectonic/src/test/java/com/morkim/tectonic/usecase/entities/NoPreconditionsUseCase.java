package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.UseCase;

public class NoPreconditionsUseCase extends UseCase {

    private boolean onExecuteCalled;

    @Override
    protected void onExecute() {
        onExecuteCalled = true;
    }

    public boolean isOnExecuteCalled() {
        return onExecuteCalled;
    }
}
