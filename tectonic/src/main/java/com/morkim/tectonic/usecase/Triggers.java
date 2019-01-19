package com.morkim.tectonic.usecase;

public interface Triggers<E extends TectonicEvent> {

    E trigger(E event, PreconditionActor preconditionActor, PrimaryActor primaryActor, ResultActor resultActor, E contextEvent);

    E trigger(E event, PrimaryActor primaryActor);

    E trigger(E event, ResultActor resultActor);

    E trigger(E event);

    <R> E map(Class<? extends UseCase<R>> cls, TectonicEvent contextEvent) throws InterruptedException, AbortedUseCase;

    E trigger(Class<? extends UseCase<?>> useCase, PreconditionActor preconditionActor);
}
