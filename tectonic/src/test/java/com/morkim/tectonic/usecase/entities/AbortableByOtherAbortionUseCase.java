package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCase;

import java.text.SimpleDateFormat;
import java.util.Set;

public class AbortableByOtherAbortionUseCase extends SimpleUseCase {

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

    }

    @Override
    protected void abortWhenAborted(Set<Class<? extends UseCase>> by) {
        super.abortWhenAborted(by);

        by.add(SimpleUseCase.class);
    }
}
