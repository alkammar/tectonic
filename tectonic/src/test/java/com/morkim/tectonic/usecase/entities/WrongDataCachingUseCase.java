package com.morkim.tectonic.usecase.entities;

public class WrongDataCachingUseCase extends SimpleUseCase {

    private WrongDataCachingUseCase.Actor actor;
    private int counter;

    @Override
    protected void onExecute() {

        StepData data = step(Actor.CACHED_DATA_KEY_1, new CacheDataListener<StepData>() {
            @Override
            public StepData onNewData() { return actor.requestData();
            }
        });

        counter++;

        OtherStepData otherData = step(counter == 1 ? Actor.CACHED_DATA_KEY_2 : Actor.CACHED_DATA_KEY_1, new CacheDataListener<OtherStepData>() {
            @Override
            public OtherStepData onNewData() { return actor.requestOtherData(); }
        });
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor {

        int CACHED_DATA_KEY_1 = 1;
        int CACHED_DATA_KEY_2 = 2;

        StepData requestData();

        OtherStepData requestOtherData();
    }
}
