package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UndoException;

import java.util.Set;

public class CompletedUseCase extends SimpleUseCase {

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        complete();
    }

    public interface Actor extends PrimaryActor<TectonicEvent, Void>, PreconditionActor<TectonicEvent> {

    }
}
