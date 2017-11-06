package com.morkim.usecase.uc;

import com.morkim.tectonic.Request;
import com.morkim.tectonic.Result;
import com.morkim.tectonic.SimpleDisposableUseCaseListener;
import com.morkim.tectonic.UseCase;
import com.morkim.usecase.model.Profile;


public class MainUseCase extends UseCase<Request, Result> {

    Profile profile = new Profile();

    @Override
    protected void onAddPrerequisites() {
        super.onAddPrerequisites();

        profile = new Profile();

        addPrerequisite(
                AuthenticateLogin.class,
                !profile.isLoggedIn(),
                new SimpleDisposableUseCaseListener());
    }

    @Override
    protected void onExecute(Request request) {

    }
}
