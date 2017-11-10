package com.morkim.tectonic;

class Prerequisite {

    Class<? extends UseCase> useCase;
    UseCaseListener listener;
    Precondition precondition;

    Prerequisite(Class<? extends UseCase> useCase, Precondition precondition, UseCaseListener listener) {

        this.useCase = useCase;
        this.precondition = precondition;
        this.listener = listener;
    }

    public Prerequisite(Precondition precondition) {
        this.precondition = precondition;
    }

    public Prerequisite otherwiseExecute(Class<? extends UseCase> useCase) {

        this.useCase = useCase;
        return this;
    }

    public void subscribe(UseCaseListener listener) {
        this.listener = listener;
    }
}
