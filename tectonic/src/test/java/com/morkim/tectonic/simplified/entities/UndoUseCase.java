package com.morkim.tectonic.simplified.entities;

public class UndoUseCase extends SimpleUseCase {

    private Actor actor;
    private boolean canFinish;

    @Override
    protected void onExecute() throws InterruptedException {
        super.onExecute();

        StepData data = actor.requestData();
        data.access();

        if (canFinish) finish();
    }

    @Override
    public void restart() {
        super.restart();

        canFinish = true;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor {

        StepData requestData();

        StepData requestOtherData();

        StepData requestAnotherData();
    }
}