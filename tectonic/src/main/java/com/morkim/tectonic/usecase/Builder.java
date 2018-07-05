package com.morkim.tectonic.usecase;

public class Builder<E> {

    private UseCase<E, ?> useCase;
    private PreconditionActor preconditionActor;
    private PrimaryActor primaryActor;
    private ResultActor resultActor;
    private Triggers<E> triggers;

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

    public Builder<E> resultActor(ResultActor<E, ?> resultActor) {
        this.resultActor = resultActor;

        return this;
    }

    public Builder<E> triggers(Triggers<E> triggers) {
        this.triggers = triggers;

        return this;
    }

    public UseCase<E, ?> build() {
        useCase.setPreconditionActor(preconditionActor);
        useCase.setPrimaryActor(primaryActor);
        useCase.setResultActor(resultActor);
        useCase.setTriggers(triggers);
        return useCase;
    }
}