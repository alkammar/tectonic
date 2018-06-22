package com.morkim.usecase.uc;

import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.Profile;

import javax.inject.Inject;


public class LogoutUser extends UseCase {

    @Inject
    Profile profile = AppInjector.getAppComponent().getProfile();

    @Inject
    User user;

    @Override
    protected void onExecute() {

        profile.setLoggedIn(false);
        user.askToLogin();
        complete();
    }

    public interface User {

        void askToLogin();
    }
}
