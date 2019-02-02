package com.morkim.tectonic.usecase;

public class UseCaseAborted extends Exception {
    private Class<? extends UseCase> useCaseClass;

    public UseCaseAborted(Class<? extends UseCase> cls) {
        useCaseClass = cls;
    }

    public Class<? extends UseCase> getUseCase() {
        return useCaseClass;
    }
}
