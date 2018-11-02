package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;

import java.util.Set;

public class AbortableByOtherCompletionUseCase extends SimpleUseCase {

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

    }

    @Override
    protected void abortWhenCompleted(Set<Class<? extends UseCase>> by) {
        super.abortWhenCompleted(by);

        by.add(CompletedUseCase.class);
    }
}
