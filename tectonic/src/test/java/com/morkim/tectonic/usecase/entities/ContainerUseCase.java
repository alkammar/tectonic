package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UndoException;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class ContainerUseCase extends SimpleUseCase {

    public static final UUID SUB_KEY = UUID.randomUUID();
    private Actor actor;

    public void addPrimaryActor(Actor primaryActor) {
        super.addPrimaryActor(primaryActor);

        this.actor = primaryActor;
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        try {
            actor.doSomething();

            execute(SUB_KEY, AbortedUseCase.class);

        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        complete();
    }

    public interface Actor extends PrimaryActor<TectonicEvent>, PreconditionActor<TectonicEvent> {

        void doSomething() throws InterruptedException, ExecutionException, UndoException;
    }
}
