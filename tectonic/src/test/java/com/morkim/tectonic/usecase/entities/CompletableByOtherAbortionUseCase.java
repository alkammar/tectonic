package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;

import java.util.Set;

public class CompletableByOtherAbortionUseCase extends SimpleUseCase {

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

    }

    @Override
    protected void completeWhenAborted(Set<Class<? extends UseCase>> by) {
        super.completeWhenAborted(by);

        by.add(SimpleUseCase.class);
    }
}
