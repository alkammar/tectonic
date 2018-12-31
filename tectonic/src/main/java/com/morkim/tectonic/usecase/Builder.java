package com.morkim.tectonic.usecase;

public class Builder {

    private UseCase<?> useCase;
    private PreconditionActor preconditionActor;
    private ResultActor[] resultActors;
    private Triggers triggers;

    public Builder useCase(Class<? extends UseCase> aClass) {
        useCase = UseCase.fetch(aClass);

        return this;
    }

    public Builder preconditionActor(PreconditionActor preconditionActor) {
        this.preconditionActor = preconditionActor;

        return this;
    }

    @SafeVarargs
    public final Builder resultActor(ResultActor<TectonicEvent, ?>... resultActors) {
        this.resultActors = resultActors;

        return this;
    }

    public Builder triggers(Triggers triggers) {
        this.triggers = triggers;

        return this;
    }

    public UseCase<?> build() {
        useCase.setPreconditionActor(preconditionActor);
        if (resultActors != null)
            for (ResultActor resultActor : resultActors) useCase.addResultActor(resultActor);
        useCase.setExecutor(triggers);
        return useCase;
    }
}
