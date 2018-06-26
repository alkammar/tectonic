package com.morkim.tectonic.simplified;

public interface Triggers<E> {

    E trigger(E event, PreconditionActor<E> preconditionActor, PrimaryActor<E, ?> primaryActor);

    E trigger(E event, PrimaryActor<E, ?> primaryActor);

    E trigger(E event, PreconditionActor<E> preconditionActor);

    E trigger(E event);
}
