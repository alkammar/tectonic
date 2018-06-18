package com.morkim.usecase.app;

import com.morkim.tectonic.simplified.Triggers;

public interface AppTrigger extends Triggers<AppTrigger.Event> {

    enum Event {
        USER_LOGOUT, LAUNCH_MAIN
    }
}
