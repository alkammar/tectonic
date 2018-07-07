package com.morkim.tectonic.usecase;

public interface Triggers<E> {

    E trigger(E event, PreconditionActor<E> preconditionActor, PrimaryActor<E, ?> primaryActor, ResultActor<E, ?> resultActor);

    E trigger(E event, PrimaryActor<E, ?> primaryActor);

    E trigger(E event, ResultActor<E, ?> resultActor);

    E trigger(E event, PreconditionActor<E> preconditionActor);

    E trigger(E event);

    <R> R trigger(Class<? extends UseCase<E, R>> cls) throws InterruptedException, AbortedUseCase;
}
