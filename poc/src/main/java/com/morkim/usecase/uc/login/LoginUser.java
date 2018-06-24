package com.morkim.usecase.uc.login;

import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.di.AppInjector;

import java.util.Set;

import javax.inject.Inject;


public class LoginUser extends UseCase<AppTrigger.Event, Void> {

    @Inject
    Authenticator authenticator;

    @Inject
    User user;

    @Override
    protected void onCreate() {
        super.onCreate();

        AppInjector.getLoginUserComponent().inject(this);
    }

    @Override
    protected void onAddPreconditions(Set<AppTrigger.Event> events) {

    }

    @Override
    protected void onExecute() throws InterruptedException {

        String password = user.askToEnterPassword();

        try {
            authenticator.validateCredentials(password);
            complete();
        } catch (InvalidLogin e) {
            user.handle(e);
            restart();
        }
    }

    public interface Authenticator {

        void validateCredentials(String password) throws InvalidLogin;
    }

    public interface User {

        String askToEnterPassword() throws InterruptedException;

        void handle(Exception e);
    }

}
