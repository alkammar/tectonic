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

        // The registration use case is a prerequisite to the login use case
        addPrerequisite(
                () -> !profile.isRegistered(),
                RegisterUser.class,
                new SimpleDisposableUseCaseListener<Result>() {
                    @Override
                    public void onComplete() {
                        // if we completed registration then no need to login, you can see that the
                        // skip flag is now updated before the execution of the login use case, so
                        // we can check it state in the onExecute method
                        skip = true;
                    }
                });
    }

    @Override
    protected void onExecute(AuthenticateLoginRequest request) {

        if (!skip && (request == null || request.password.isEmpty()))
            requestInput(PASSWORD);
        else {
            // Login is done, finish the use case so other blocking use cases can continue their
            // execution
            profile.setLoggedIn(true);
            finish();
        }
    }
}
