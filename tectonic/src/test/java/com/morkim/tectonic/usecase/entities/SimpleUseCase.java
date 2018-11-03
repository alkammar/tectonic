package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;

import java.util.Set;

public class SimpleUseCase extends UseCase<Void> {

    private int onCreateCalledCount;
    private int onExecuteCalledCount;
    private boolean onCheckPreconditionsCalled;
    private boolean abortCalled;
    private Actor actor;

    @Override
    protected void onCreate() {
        super.onCreate();

        onCreateCalledCount++;
    }

    @Override
    protected void onAddPreconditions(Set<Class<? extends UseCase<?>>> useCases) {
        super.onAddPreconditions(useCases);

        onCheckPreconditionsCalled = true;
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        onExecuteCalledCount++;
    }

    public boolean isOnExecuteCalled() {
        return onExecuteCalledCount > 0;
    }

    @Override
    public void abort() {
        super.abort();

        abortCalled = true;
    }

    public int getOnExecuteCalledCount() {
        return onExecuteCalledCount;
    }

    public boolean isOnCheckPreconditionsCalled() {
        return onCheckPreconditionsCalled;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public int getOnCreateCalledCount() {
        return onCreateCalledCount;
    }

    public boolean isOnCreateCalled() {
        return onCreateCalledCount > 0;
    }

    public boolean isAbortCalled() {
        return abortCalled;
    }

    public interface Actor extends PrimaryActor<Integer, Void> {
    }
}
