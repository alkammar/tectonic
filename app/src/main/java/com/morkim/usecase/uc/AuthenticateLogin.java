package com.morkim.usecase.uc;

import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleDisposableUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.Profile;

import javax.inject.Inject;


public class AuthenticateLogin extends UseCase<AuthenticateLoginRequest, Result> {

    public static final int PASSWORD = 1;

    @Inject
    Profile profile = AppInjector.getAppComponent().getProfile();

    private boolean skip;

    @Override
    protected void onAddPrerequisites() {
        super.onAddPrerequisites();

        addPrerequisite(
                () -> !profile.isRegistered(),
                RegisterUser.class,
                new SimpleDisposableUseCaseListener<Result>() {
                    @Override
                    public void onComplete() {
                        skip = true;
                    }
                });
    }

    @Override
    protected void onExecute(AuthenticateLoginRequest request) {

        if (!skip && (request == null || request.password.isEmpty()))
            requestInput(PASSWORD);
        else {
            profile.setLoggedIn(true);
            finish();
        }
    }
}
