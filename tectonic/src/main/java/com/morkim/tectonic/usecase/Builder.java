package com.morkim.tectonic.usecase;

/**
 * Convenient builder for a configuring and executing a use case
 */
public class Builder {

    private UseCase<?> useCase;
    private SecondaryActor preconditionActor;
    private ResultActor[] resultActors;
    private Triggers triggers;
    private UseCase container;
    private ResultActor containerResultActor;

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
    Builder preconditionActor(SecondaryActor preconditionActor) {
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
     * Configure the triggers used by the system to trigger the use cases. This is used to trigger
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
     * Configure the container/context use case that the use case is being run as part of its execution
     *
     * @param container the container/context use case
     * @return builder
     */
    Builder container(UseCase container) {
        this.container = container;

        return this;
    }

    /**
     * Configure the container/context result actor that will observe the use case termination callbacks
     *
     * @param containerResultActor the container/context result actor
     * @return builder
     */
    Builder containerResultActor(ResultActor containerResultActor) {
        this.containerResultActor = containerResultActor;

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
        useCase.setContainer(container);
        useCase.setContainerResultActor(containerResultActor);
        return useCase;
    }
}
