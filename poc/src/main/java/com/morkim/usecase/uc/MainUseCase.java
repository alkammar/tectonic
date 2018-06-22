package com.morkim.usecase.uc;

import android.os.SystemClock;

import com.morkim.tectonic.simplified.PrimaryActor;
import com.morkim.tectonic.simplified.UseCase;

import javax.inject.Inject;


public class MainUseCase extends UseCase<String> {

    private static final int STEP = 1;

    @Inject
    Authenticator authenticator;

    @Inject
    User user;

    @Override
    protected boolean onCheckPreconditions() {
        return authenticator.checkLogin();
    }

    @Override
    protected void onExecute() {

        for (int i = 0; i < 100 / STEP; i++) {
            SystemClock.sleep(50);

            user.updateResult("" + (i + 1) * STEP);
        }

        complete("Final data sent by the main use case");
    }

    public interface Authenticator {

        boolean checkLogin();
    }

    public interface User extends PrimaryActor<String> {

        void updateResult(String data);
    }
}
