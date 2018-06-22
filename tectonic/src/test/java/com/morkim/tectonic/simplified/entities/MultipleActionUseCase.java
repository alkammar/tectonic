package com.morkim.tectonic.simplified.entities;

public class MultipleActionUseCase extends SimpleUseCase {

    private Actor actor;

    @Override
    protected void onExecute() throws InterruptedException {
        super.onExecute();

        StepData data1 = actor.requestData1();
        data1.access();

        StepData data2 = actor.requestData2();
        data2.access();

        complete();
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor {

        StepData requestData1() throws InterruptedException;

        StepData requestData2() throws InterruptedException;
    }
}
