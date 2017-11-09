package com.morkim.usecase.uc;

import com.morkim.tectonic.Request;
import com.morkim.tectonic.Result;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.Profile;

import javax.inject.Inject;


public class LogoutUser extends UseCase<Request, Result> {

    @Inject
    Profile profile = AppInjector.getAppComponent().getProfile();

    @Override
    protected void onExecute(Request request) {

        profile.setLoggedIn(false);
        finish();
    }
}
