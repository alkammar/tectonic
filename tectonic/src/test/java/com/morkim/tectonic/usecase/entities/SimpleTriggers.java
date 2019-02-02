package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.Builder;
import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.ResultActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.tectonic.usecase.UseCase;

public class SimpleTriggers implements Triggers<TectonicEvent> {
    @Override
    public TectonicEvent trigger(TectonicEvent event, PreconditionActor preconditionActor, ResultActor resultActor, TectonicEvent contextEvent) {
        if (event == AbortedUseCase.EVENT) {
            new Builder()
                    .useCase(AbortedUseCase.class)
                    .resultActor(resultActor)
                    .preconditionActor(preconditionActor)
                    .triggers(this)
                    .build()
                    .execute(event);
        }

        return event;
    }

    @Override
    public TectonicEvent trigger(TectonicEvent event, ResultActor resultActor) {
        return trigger(event, null, resultActor, null);
    }

    @Override
    public TectonicEvent trigger(TectonicEvent event) {
        return trigger(event, null, null, null);
    }

    @Override
    public TectonicEvent map(Class<? extends UseCase> cls) {
        if (CompletedUseCase.class.equals(cls)) return CompletedPreconditionsUseCase.SUCCESSFUL_EVENT;
        if (SimpleUseCase.class.equals(cls)) return FailingPreconditionsUseCase.FAILING_EVENT;
        if (AbortedUseCase.class.equals(cls)) return AbortedUseCase.EVENT;
        return null;
    }
}
