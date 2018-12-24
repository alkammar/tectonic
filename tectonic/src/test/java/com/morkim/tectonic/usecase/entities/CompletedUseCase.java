package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UndoException;

public class CompletedUseCase extends SimpleUseCase {

    private Actor actor;

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        complete();
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor extends PrimaryActor<TectonicEvent, Void>, PreconditionActor<TectonicEvent> {

    }
}
