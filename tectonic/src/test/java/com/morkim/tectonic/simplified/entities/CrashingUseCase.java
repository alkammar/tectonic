package com.morkim.tectonic.simplified.entities;

public class CrashingUseCase extends SimpleUseCase {

    public CrashingUseCase() {
        super();

        throw new RuntimeException();
    }
}
