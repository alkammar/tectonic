package com.morkim.usecase.uc;

import android.os.SystemClock;
import android.util.Log;

import com.morkim.tectonic.Request;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.model.Profile;


public class MainUseCase extends UseCase<Request, MainUseCaseResult> {

    Profile profile = new Profile();

    @Override
    protected void onAddPrerequisites() {
        super.onAddPrerequisites();

        profile = new Profile();

        addPrerequisite(
                AuthenticateLogin.class,
                !profile.isLoggedIn());
    }

    @Override
    protected void onExecute(Request request) {

        Log.i(this.getClass().getSimpleName(), "onExecute : " + Thread.currentThread().getName());

        for (int i = 0; i < 10; i++) {
            SystemClock.sleep(1000);

            MainUseCaseResult result = new MainUseCaseResult();
            result.data = "" + (i + 1) * 10;
            updateSubscribers(result);
        }

        MainUseCaseResult result = new MainUseCaseResult();
        result.data = "Some data sent by the main use case";
        updateSubscribers(result);

        finish();
    }
}
