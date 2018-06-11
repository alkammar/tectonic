package com.morkim.tectonic.entities;

import com.morkim.tectonic.Request;
import com.morkim.tectonic.UseCase;

public class CacheableTestUseCase extends UseCase<Request, TestResult> {

    private static final int MY_CACHABLE_DATA = 0;

    private Request request;
    private CacheableData cacheableData;

    private UserActor userActor;

    @Override
    protected void onExecute(Request request) {
        this.request = request;

        TestResult result = new TestResult();
        result.request = request;

        cacheableData = cache(MY_CACHABLE_DATA, new OnNoCacheListener<CacheableData>() {
            @Override
            public CacheableData onNoCache() { return userActor.askToProvideData();
            }
        });

        userActor.doYourThing();

        notifySubscribers(result);

        finish();
    }

    @Override
    protected void onUndo(Request request, TestResult oldResult) {

        finish();
    }

    public Request getRequest() {
        return request;
    }

    public CacheableData getCacheableData() {
        return cacheableData;
    }

    public void setUserActor(UserActor userActor) {
        this.userActor = userActor;
    }
}
