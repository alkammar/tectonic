package com.morkim.tectonic.usecase.entities;

import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.PrimaryActor;

public class InterruptableUseCase extends SimpleUseCase {

    private Actor actor;

    @Override
    protected void onExecute() throws InterruptedException {
        super.onExecute();

        Thread.sleep(200);

        complete();
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor extends PrimaryActor<Integer, Void>, PreconditionActor<Integer> {

    }
}
