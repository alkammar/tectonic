package com.morkim.tectonic.simplified.entities;

import com.morkim.tectonic.simplified.PreconditionActor;
import com.morkim.tectonic.simplified.PrimaryActor;

public class CompletedUseCase extends SimpleUseCase {

    private Actor actor;

    @Override
    protected void onExecute() throws InterruptedException {
        super.onExecute();

        complete();
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor extends PrimaryActor<Integer, Void>, PreconditionActor<Integer, Void> {

    }
}
