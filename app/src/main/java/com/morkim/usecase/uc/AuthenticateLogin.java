package com.morkim.usecase.uc;

import android.util.Log;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.Profile;

import javax.inject.Inject;


public class AuthenticateLogin extends UseCase<AuthenticateLoginRequest, Result> {

    public static final int PASSWORD = 1;

    @Inject
    Profile profile = AppInjector.getAppComponent().getProfile();

    @Override
    protected void onExecute(AuthenticateLoginRequest request) {

        Log.i(this.getClass().getSimpleName(), "onExecute : " + Thread.currentThread().getName());

        if (request == null || request.password.isEmpty())
            requestInput(PASSWORD);
        else {
            profile.setLoggedIn(true);
            finish();
        }
    }
}
