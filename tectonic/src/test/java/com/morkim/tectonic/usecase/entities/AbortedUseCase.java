package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.UndoException;

public class AbortedUseCase extends SimpleUseCase {

    public static final TectonicEvent EVENT = new TectonicEvent() {};

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        Thread.sleep(200);

        abort();
    }
}
