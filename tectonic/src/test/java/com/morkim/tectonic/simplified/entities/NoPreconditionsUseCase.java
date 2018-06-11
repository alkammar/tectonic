package com.morkim.tectonic.simplified.entities;

import com.morkim.tectonic.simplified.UseCase;

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
