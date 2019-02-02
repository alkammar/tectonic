package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.UndoException;

public class SubUseCase extends SimpleUseCase {

    public static boolean abort;

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        if (abort) abort();
        else complete();
    }
}
