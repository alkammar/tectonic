package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.Random;
import com.morkim.tectonic.usecase.UndoException;

import java.util.Set;

public class RealRandomActionsUseCase extends SimpleUseCase {

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

        StepData data1 = actor.requestData1();
        StepData data2 = actor.requestData2();
        StepData data3 = actor.requestData3();

        validateData1(data1);
        validateData2(data2);
        validateData3(data3);

        complete();
    }

    private boolean validateData1(StepData data1) {
        data1.access();
        return data1 != null;
    }

    private boolean validateData2(StepData data2) {
        data2.access();
        return data2 != null;
    }

    private boolean validateData3(StepData data3) {
        data3.access();
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

        StepData requestData1() throws InterruptedException, UndoException;

        StepData requestData2() throws InterruptedException, UndoException;

        StepData requestData3() throws InterruptedException, UndoException;
    }
}
