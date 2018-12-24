package com.morkim.tectonic.usecase.entities;

public class NonCachingUseCase extends CachingUseCase {

    @Override
    protected void onExecute() {

        StepData data = actor.requestData();
        data.access();
    }
}
