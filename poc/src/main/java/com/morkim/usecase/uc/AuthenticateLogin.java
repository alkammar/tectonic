package com.morkim.usecase.uc;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleDisposableUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.Profile;

import javax.inject.Inject;


public class AuthenticateLogin extends UseCase<AuthenticateLoginRequest, Result> {

    public static final int USER = 1;

    public static final int PASSWORD = 1;

    @Inject
    Profile profile = AppInjector.getAppComponent().getProfile();

    private boolean skip;

    @Override
    protected void onAddPrerequisites() {
        super.onAddPrerequisites();

        // The registration use case is a prerequisite to the login use case
        addPrerequisite(
                () -> !profile.isRegistered(),
                RegisterUser.class
        );
    }

    @Override
    protected void onExecute(AuthenticateLoginRequest request) {

        if (skip)
            completeLogin();
        else if (request == null || request.password.isEmpty())
            requestAction(USER, PASSWORD);
        else if (!request.password.equals("asdf"))
//            throw new InvalidLogin();
            ;
        else
            completeLogin();
    }

    private void completeLogin() {
        // Login is done, finish the use case so other blocking use cases can continue their
        // execution
        profile.setLoggedIn(true);
        finish();
    }

}
