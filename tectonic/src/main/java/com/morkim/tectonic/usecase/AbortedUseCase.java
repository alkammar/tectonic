package com.morkim.tectonic.usecase;

public class AbortedUseCase extends Exception {
    private Class<? extends UseCase> useCaseClass;

    public <R> AbortedUseCase(Class<? extends UseCase> cls) {
        useCaseClass = cls;
    }

    public Class<? extends UseCase> getUseCase() {
        return useCaseClass;
    }
}
