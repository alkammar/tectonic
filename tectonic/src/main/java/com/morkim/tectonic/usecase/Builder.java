package com.morkim.tectonic.usecase;

/**
 * Convenient builder for a configuring and executing a use case
 */
public class Builder {

    private UseCase<?> useCase;
    private PreconditionActor preconditionActor;
    private ResultActor[] resultActors;
    private Triggers triggers;

    /**
     * The use case to build
     *
     * @param cls the use case class to build
     * @return builder
     */
    public Builder useCase(Class<? extends UseCase> cls) {
        useCase = UseCase.fetch(cls);

        return this;
    }

    /**
     * Configure the precondition actor for this use case
     *
     * @param preconditionActor the actor
     * @return builder
     */
    public Builder preconditionActor(PreconditionActor preconditionActor) {
        this.preconditionActor = preconditionActor;

        return this;
    }

    /**
     * Configure the result actors to receive results from this use case
     *
     * @param resultActors an array of result actors
     * @return builder
     */
    @SafeVarargs
    public final Builder resultActor(ResultActor<TectonicEvent, ?>... resultActors) {
        this.resultActors = resultActors;

        return this;
    }

    /**
     * Configure the triggers used by the system to trigger the use cases. This is use to trigger
     * sub use cases
     *
     * @param triggers system triggers
     * @return builder
     */
    public Builder triggers(Triggers triggers) {
        this.triggers = triggers;

        return this;
    }

    /**
     * Builds the use case with the selected configurations
     *
     * @return the use case ready for execution
     */
    public UseCase<?> build() {
        useCase.setPreconditionActor(preconditionActor);
        if (resultActors != null)
            for (ResultActor resultActor : resultActors) useCase.addResultActor(resultActor);
        useCase.setExecutor(triggers);
        return useCase;
    }
}
