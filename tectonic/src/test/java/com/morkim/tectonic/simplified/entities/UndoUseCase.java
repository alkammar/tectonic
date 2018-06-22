package com.morkim.tectonic.simplified.entities;

import com.morkim.tectonic.simplified.PrimaryActor;

public class UndoUseCase extends SimpleUseCase {

    private Actor actor;

    @Override
    protected void onExecute() throws InterruptedException {
        super.onExecute();

        StepData data1 = actor.requestData();
        data1.access();

        StepData data2 = actor.requestOtherData();
        data2.access();

        StepData data3 = actor.requestAnotherData();
        data3.access();

        complete();
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor extends PrimaryActor<Void> {

        StepData requestData() throws InterruptedException;

        StepData requestOtherData() throws InterruptedException;

        StepData requestAnotherData() throws InterruptedException;
    }
}
