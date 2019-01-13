package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.Random;
import com.morkim.tectonic.usecase.UndoException;

import java.util.Set;

public class RandomActionsUseCase extends SimpleUseCase {

    private Actor actor;

    private boolean data1Validated;
    private boolean data2Validated;
    private boolean data3Validated;

    @Override
    protected void onAddPrimaryActors(Set<PrimaryActor> actors) {
        super.onAddPrimaryActors(actors);

        actors.add(actor);
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        Random<StepData> data1 = actor.requestData1();
        Random<StepData> data2 = actor.requestData2();
        Random<StepData> data3 = actor.requestData3();

        actor.confirm();

        validateData1(data1);
        validateData2(data2);
        validateData3(data3);

        complete();
    }

    private boolean validateData1(Random<StepData> data1) {
        data1Validated = data1.isSet();
        if (data1.isSet()) data1.value().access();
        return data1 != null;
    }

    private boolean validateData2(Random<StepData> data2) {
        data2Validated = data2.isSet();
        if (data2.isSet()) data2.value().access();
        return data2 != null;
    }

    private boolean validateData3(Random<StepData> data3) {
        data3Validated = data3.isSet();
        if (data3.isSet()) data3.value().access();
        return data3 != null;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public boolean isData1Validated() {
        return data1Validated;
    }

    public boolean isData2Validated() {
        return data2Validated;
    }

    public boolean isData3Validated() {
        return data3Validated;
    }

    public interface Actor extends PrimaryActor {

        Random<StepData> requestData1() throws InterruptedException;

        Random<StepData> requestData2() throws InterruptedException;

        Random<StepData> requestData3() throws InterruptedException;

        void confirm() throws InterruptedException, UndoException;
    }
}
