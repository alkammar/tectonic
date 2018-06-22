package com.morkim.usecase.app;

import com.morkim.tectonic.simplified.Triggers;
import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.uc.MainUseCase;

public class AppTrigger implements Triggers<AppTrigger.Event> {

    @Override
    public void trigger(Event event) {

        switch (event) {

            case LAUNCH_MAIN:

                UseCase.fetch(MainUseCase.class).execute();
                break;
            case USER_LOGOUT:
                break;
        }
    }

    public enum Event {
        LAUNCH_MAIN,
        USER_LOGOUT,
    }
}
