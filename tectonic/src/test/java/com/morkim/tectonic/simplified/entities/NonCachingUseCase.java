package com.morkim.tectonic.simplified.entities;

public class NonCachingUseCase extends CachingUseCase {

    @Override
    protected void onExecute() {

        StepData data = actor.requestData();
        data.access();
    }
}
