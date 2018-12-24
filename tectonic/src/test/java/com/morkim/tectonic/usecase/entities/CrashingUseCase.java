package com.morkim.tectonic.usecase.entities;

public class CrashingUseCase extends SimpleUseCase {

    public CrashingUseCase() {
        super();

        throw new RuntimeException();
    }
}
