package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UndoException;

import java.util.concurrent.ExecutionException;

public class CompletedUseCase extends SimpleUseCase {

    private Actor actor;

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        try {
            if (actor != null) actor.doSomething();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        complete();
    }

    @Override
    public void addPrimaryActor(PrimaryActor primaryActor) {
        super.addPrimaryActor(primaryActor);
        actor = (Actor) primaryActor;
    }

    public interface Actor extends PrimaryActor<TectonicEvent> {

        void doSomething() throws InterruptedException, ExecutionException, UndoException;
    }
}
