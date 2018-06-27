package com.morkim.usecase.uc.login;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.di.AppInjector;

import java.util.Set;

import javax.inject.Inject;


public class LoginUser extends UseCase<AppTrigger.Event, Void> {

    @Inject
    Authenticator authenticator;

    @Inject
    UI ui;

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

        String password = ui.askForPassword();

        try {
            authenticator.validateCredentials(password);
            complete();
        } catch (InvalidLogin e) {
            ui.show(e);
            restart();
        }
    }

    public interface Authenticator {

        void validateCredentials(String password) throws InvalidLogin;
    }

    public interface UI extends PrimaryActor<AppTrigger.Event, Void> {

        String askForPassword() throws InterruptedException;

        void show(Exception e);
    }

}
