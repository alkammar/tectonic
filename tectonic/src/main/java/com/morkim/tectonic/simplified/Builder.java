package com.morkim.tectonic.simplified;

public class Builder<E> {

    private UseCase<E, ?> useCase;
    private PreconditionActor preconditionActor;
    private PrimaryActor primaryActor;

    public Builder<E> useCase(Class<? extends UseCase> aClass) {
        useCase = UseCase.fetch(aClass);

        return this;
    }

    public Builder<E> preconditionActor(PreconditionActor<E> preconditionActor) {
        this.preconditionActor = preconditionActor;

        return this;
    }

    public Builder<E> primaryActor(PrimaryActor<E, ?> primaryActor) {
        this.primaryActor = primaryActor;

        return this;
    }

    public UseCase<E, ?> build() {
        useCase.setPreconditionActor(preconditionActor);
        useCase.setPrimaryActor(primaryActor);
        return useCase;
    }
}
