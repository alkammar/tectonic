package com.morkim.tectonic.simplified.entities;

import com.morkim.tectonic.simplified.PrimaryActor;

public class FinishedUseCase extends SimpleUseCase {

    private Actor actor;

    @Override
    protected void onExecute() throws InterruptedException {
        super.onExecute();

        finish();
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public interface Actor extends PrimaryActor {

    }
}
