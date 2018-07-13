package com.morkim.tectonic.usecase.entities;

public class ActorCachingUseCase extends SimpleUseCase {

    private Actor actor;
    private boolean canFinish;

    @Override
    protected void onExecute() throws InterruptedException {
        super.onExecute();

        StepData data = actor.requestData();
        data.access();

        if (canFinish) complete();
    }

    @Override
    public void retry() {
        super.retry();

        canFinish = true;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor {

        StepData requestData() throws InterruptedException;
    }
}
