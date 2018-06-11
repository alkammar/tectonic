package com.morkim.tectonic.simplified.entities;

public class CachingUseCase extends SimpleUseCase {

    protected Actor actor;

    @Override
    protected void onExecute() {
        super.onExecute();

        CacheData data = cache(Actor.CACHED_DATA_KEY_1, new CacheDataListener<CacheData>() {
            @Override
            public CacheData onNewData() { return actor.requestData();
            }
        });
        data.access();
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor {

        int CACHED_DATA_KEY_1 = 1;

        CacheData requestData();
    }
}
