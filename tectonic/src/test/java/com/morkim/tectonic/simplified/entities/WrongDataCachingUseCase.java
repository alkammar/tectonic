package com.morkim.tectonic.simplified.entities;

public class WrongDataCachingUseCase extends SimpleUseCase {

    private WrongDataCachingUseCase.Actor actor;
    private int counter;

    @Override
    protected void onExecute() {

        CacheData data = cache(Actor.CACHED_DATA_KEY_1, new CacheDataListener<CacheData>() {
            @Override
            public CacheData onNewData() { return actor.requestData();
            }
        });

        counter++;

        OtherCacheData otherData = cache(counter == 1 ? Actor.CACHED_DATA_KEY_2 : Actor.CACHED_DATA_KEY_1, new CacheDataListener<OtherCacheData>() {
            @Override
            public OtherCacheData onNewData() { return actor.requestOtherData(); }
        });
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor {

        int CACHED_DATA_KEY_1 = 1;
        int CACHED_DATA_KEY_2 = 2;

        CacheData requestData();

        OtherCacheData requestOtherData();
    }
}
