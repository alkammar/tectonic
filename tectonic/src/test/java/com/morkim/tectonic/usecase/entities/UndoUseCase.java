package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.UndoException;

public class UndoUseCase extends SimpleUseCase {

    private Actor actor;

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
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

    public interface Actor extends PrimaryActor<Integer, Void> {

        StepData requestData() throws InterruptedException, UndoException;

        StepData requestOtherData() throws InterruptedException, UndoException;

        StepData requestAnotherData() throws InterruptedException, UndoException;
    }
}
