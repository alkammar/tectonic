package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.AbortedUseCase;
import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.ResultActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.tectonic.usecase.UseCase;

public class SimpleTriggers implements Triggers<TectonicEvent> {
    @Override
    public TectonicEvent trigger(TectonicEvent event, PreconditionActor preconditionActor, PrimaryActor primaryActor, ResultActor resultActor, TectonicEvent contextEvent) {
        return event;
    }

    @Override
    public TectonicEvent trigger(TectonicEvent event, PrimaryActor primaryActor) {
        return trigger(event, null, primaryActor, null, null);
    }

    @Override
    public TectonicEvent trigger(TectonicEvent event, ResultActor resultActor) {
        return trigger(event, null, null, resultActor, null);
    }

    @Override
    public TectonicEvent trigger(TectonicEvent event) {
        return trigger(event, null, null, null, null);
    }

    @Override
    public <R> R trigger(Class<? extends UseCase<R>> cls, TectonicEvent contextEvent) throws InterruptedException, AbortedUseCase {
        return null;
    }

    @Override
    public TectonicEvent trigger(Class<? extends UseCase<?>> useCase, PreconditionActor preconditionActor) {
        if (CompletedUseCase.class.equals(useCase))
            return trigger(CompletedPreconditionsUseCase.SUCCESSFUL_EVENT, preconditionActor, null, null, null);
        else if (SimpleUseCase.class.equals(useCase))
            return trigger(FailingPreconditionsUseCase.FAILING_EVENT, preconditionActor, null, null, null);
        return null;
    }
}
