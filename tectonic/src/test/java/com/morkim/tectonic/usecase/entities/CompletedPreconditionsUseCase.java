package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.UndoException;

import java.util.Set;

public class CompletedPreconditionsUseCase extends SimpleUseCase {

    public static final int SUCCESSFUL_EVENT = 11;

    @Override
    protected void onAddPreconditions(Set<Integer> events) {
        events.add(SUCCESSFUL_EVENT);
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        complete();
    }
}
