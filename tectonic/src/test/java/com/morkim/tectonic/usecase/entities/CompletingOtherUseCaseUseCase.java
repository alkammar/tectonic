package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.UndoException;

public class CompletingOtherUseCaseUseCase extends SimpleUseCase {

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        complete();
    }
}
