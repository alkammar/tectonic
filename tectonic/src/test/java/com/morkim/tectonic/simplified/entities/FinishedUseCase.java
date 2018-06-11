package com.morkim.tectonic.simplified.entities;

public class FinishedUseCase extends SimpleUseCase {

    @Override
    protected void onExecute() {
        super.onExecute();

        finish();
    }
}
