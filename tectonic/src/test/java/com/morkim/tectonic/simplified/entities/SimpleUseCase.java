package com.morkim.tectonic.simplified.entities;

import com.morkim.tectonic.simplified.UseCase;

public class SimpleUseCase extends UseCase {

    private int onExecuteCalledCount;
    private boolean onCheckPreconditionsCalled;

    @Override
    protected boolean onCheckPreconditions() {
        onCheckPreconditionsCalled = true;
        return true;
    }

    @Override
    protected void onExecute() {
        onExecuteCalledCount++;
    }

    public boolean isOnExecuteCalled() {
        return onExecuteCalledCount > 0;
    }

    public int getOnExecuteCalledCount() {
        return onExecuteCalledCount;
    }

    public boolean isOnCheckPreconditionsCalled() {
        return onCheckPreconditionsCalled;
    }
}
