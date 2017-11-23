package com.morkim.usecase.uc;

import android.os.SystemClock;

import com.morkim.tectonic.Request;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.Profile;

import javax.inject.Inject;


public class MainUseCase extends UseCase<Request, MainUseCaseResult> {

    private static final int STEP = 1;

    @Inject
    Profile profile = AppInjector.getAppComponent().getProfile();

    @Override
    protected void onAddPrerequisites() {
        super.onAddPrerequisites();

        // The login use case is a prerequisite to the main use case, and it is preconditioned with
        // this isLoggedIn flag.
        // There is override to this method where you can omit the precondition
        addPrerequisite(() -> !profile.isLoggedIn(), AuthenticateLogin.class);
    }

    @Override
    protected void onExecute(Request request) {

        for (int i = 0; i < 100 / STEP; i++) {
            SystemClock.sleep(50);

            MainUseCaseResult result = new MainUseCaseResult();
            result.data = "" + (i + 1) * STEP;
            updateSubscribers(result);
        }

        MainUseCaseResult result = new MainUseCaseResult();
        result.data = "Some data sent by the main use case";
        updateSubscribers(result);

        finish();
    }

    @Override
    protected boolean supportsCaching() {
        return true;
    }
}
