package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.UndoException;

import java.util.Set;

public class ActorCachingUseCase extends SimpleUseCase {

    private Actor actor;
    private boolean canFinish;

    @Override
    protected void onAddPrimaryActors(Set<PrimaryActor> actors) {
        super.onAddPrimaryActors(actors);

        actors.add(actor);
    }

    @Override
    protected void onExecute() throws InterruptedException, UndoException {
        super.onExecute();

        StepData data = actor.requestData();
        data.access();

        if (canFinish) complete();
    }

//    @Override
//    public void retry() throws InterruptedException {
//        super.retry();
//
//        canFinish = true;
//    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor extends PrimaryActor {

        StepData requestData() throws InterruptedException, UndoException;
    }
}
