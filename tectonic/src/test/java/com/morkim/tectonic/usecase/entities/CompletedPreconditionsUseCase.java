package com.morkim.tectonic.usecase.entities;

import java.util.Set;

public class CompletedPreconditionsUseCase extends SimpleUseCase {

    public static final Integer SUCCESSFUL_EVENT = 11;

    @Override
    protected void onAddPreconditions(Set<Integer> events) {
        events.add(SUCCESSFUL_EVENT);
    }

    @Override
    protected void onExecute() throws InterruptedException {
        super.onExecute();

        complete();
    }
}
