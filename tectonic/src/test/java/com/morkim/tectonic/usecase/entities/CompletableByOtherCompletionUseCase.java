package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;

import java.util.Set;

public class CompletableByOtherCompletionUseCase extends SimpleUseCase {

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

    }

    @Override
    protected void completedBy(Set<Class<? extends UseCase>> by) {
        super.completedBy(by);

        by.add(CompletingOtherUseCaseUseCase.class);
    }
}
