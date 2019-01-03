package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.UndoException;

import java.util.Set;

public class MultipleActionUseCase extends SimpleUseCase {

    private Actor actor;

    @Override
    protected void onAddPrimaryActors(Set<PrimaryActor> actors) {
        super.onAddPrimaryActors(actors);

        actors.add(actor);
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
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

    public interface Actor extends PrimaryActor {

        StepData requestData1() throws InterruptedException, UndoException;

        StepData requestData2() throws InterruptedException, UndoException;
    }
}
