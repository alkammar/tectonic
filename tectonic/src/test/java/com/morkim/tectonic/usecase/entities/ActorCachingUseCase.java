package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.UndoException;

public class ActorCachingUseCase extends SimpleUseCase {

    private Actor actor;
    private boolean canFinish;

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
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

        StepData requestData() throws InterruptedException, UndoException;
    }
}
