package com.morkim.usecase.uc;

import android.os.SystemClock;

import com.morkim.tectonic.Request;
import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.Profile;

import javax.inject.Inject;


public class MainUseCase extends UseCase {

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

        user.updateResult("Some data sent by the main use case");

        finish();
    }

    public interface Authenticator {

        boolean checkLogin();
    }

    public interface User {

        void updateResult(String data);
    }
}
