package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.UseCase;

import java.util.Set;

public class SimpleUseCase extends UseCase<Integer, Void> {

    private int onCreateCalledCount;
    private int onExecuteCalledCount;
    private boolean onCheckPreconditionsCalled;
    private Actor actor;

    @Override
    protected void onCreate() {
        super.onCreate();

        onCreateCalledCount++;
    }

    @Override
    protected void onAddPreconditions(Set<Integer> events) {
        onCheckPreconditionsCalled = true;
    }

    @Override
    protected void onExecute() throws InterruptedException {
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

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public int getOnCreateCalledCount() {
        return onCreateCalledCount;
    }

    public boolean isOnCreateCalled() {
        return onCreateCalledCount > 0;
    }

    public interface Actor extends PrimaryActor<Integer, Void> {
    }
}
