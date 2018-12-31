package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.Actor;
import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.SecondaryActor;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SimpleUseCase extends UseCase<Void> {

    private int onCreateCalledCount;
    private int onExecuteCalledCount;
    private boolean onCheckPreconditionsCalled;
    private boolean abortCalled;
    private List<PrimaryActor> primaryActors = new ArrayList<>();
    private List<SecondaryActor> secondaryActors = new ArrayList<>();
    private Actor unknownActor;

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
    protected void onAddPrimaryActors(Set<PrimaryActor> actors) {
        actors.addAll(primaryActors);
    }

    @Override
    protected void onAddSecondaryActors(Set<SecondaryActor> actors) {
        actors.addAll(secondaryActors);
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

    public void setUnknownActor(Actor unknownActor) {
        this.unknownActor = unknownActor;
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

    public void addPrimaryActor(PrimaryActor primaryActor) {
        this.primaryActors.add(primaryActor);
    }

    public void addSecondaryActor(SecondaryActor secondaryActor) {
        this.secondaryActors.add(secondaryActor);
    }

    public interface SimpleActor extends PrimaryActor<Integer, Void> {

    }
}
