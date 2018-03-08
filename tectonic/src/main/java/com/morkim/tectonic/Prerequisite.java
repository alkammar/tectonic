package com.morkim.tectonic;

class Prerequisite {

    PreAction preAction;
    Class<? extends UseCase> useCase;
    UseCaseListener listener;
    Precondition precondition;

    Prerequisite(Class<? extends UseCase> useCase, Precondition precondition, UseCaseListener listener, PreAction preAction) {

        this.useCase = useCase;
        this.precondition = precondition;
        this.listener = listener;
        this.preAction = preAction;
    }

    public void subscribe(UseCaseListener listener) {
        this.listener = listener;
    }
}
