package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;

import java.util.Set;

public class CompletedPreconditionsUseCase extends SimpleUseCase {

    public static final TectonicEvent SUCCESSFUL_EVENT = new TectonicEvent() {};

    @Override
    protected void onAddPreconditions(Set<Class<? extends UseCase>> useCases) {
        super.onAddPreconditions(useCases);

//        useCases.add(SUCCESSFUL_EVENT);
        useCases.add(CompletedUseCase.class);
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        Thread.sleep(200);

        complete();
    }
}
