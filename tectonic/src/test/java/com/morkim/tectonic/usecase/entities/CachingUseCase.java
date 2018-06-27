package com.morkim.tectonic.usecase.entities;

public class CachingUseCase extends SimpleUseCase {

    protected Actor actor;

    @Override
    protected void onExecute() throws InterruptedException {
        super.onExecute();

        StepData data = step(Actor.STEP_DATA_KEY_1, new CacheDataListener<StepData>() {
            @Override
            public StepData onNewData() { return actor.requestData();
            }
        });
        data.access();
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor {

        int STEP_DATA_KEY_1 = 1;

        StepData requestData();
    }
}
