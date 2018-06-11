package com.morkim.tectonic.simplified.entities;

public class NonCachingUseCase extends CachingUseCase {

    @Override
    protected void onExecute() {

        CacheData data = actor.requestData();
        data.access();
    }
}
