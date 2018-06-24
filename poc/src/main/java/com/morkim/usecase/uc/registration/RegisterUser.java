package com.morkim.usecase.uc.registration;

import com.morkim.tectonic.simplified.PrimaryActor;
import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.model.Profile;

import javax.inject.Inject;


public class RegisterUser extends UseCase {

    @Inject
    User user;

    @Inject
    Profile profile = AppInjector.getAppComponent().getProfile();

    @Override
    protected void onExecute() throws InterruptedException {

        String email = null;
        String password = null;
        String mobile = null;

        try {
            email = validateEmail(user.askToEnterEmail());
        } catch (EmptyEmail | InvalidEmail e) {
            user.handle(e);
        }

        try {
            password = validatePassword(user.askToEnterPassword());
        } catch (EmptyPassword e) {
            user.handle(e);
        }

        try {
            mobile = validatePassword(user.askToEnterMobile());
        } catch (EmptyPassword e) {
            user.handle(e);
        }

        if (email != null && password != null && mobile != null) {
            profile.setRegistered(true);
            complete();
        } else
            restart();
    }

    private String validatePassword(String password) throws EmptyPassword {
        if (password.isEmpty()) throw new EmptyPassword();
        return password;
    }

    private String validateEmail(String email) throws EmptyEmail, InvalidEmail {
        if (email.isEmpty()) throw new EmptyEmail();
        if (!email.contains("@")) throw new InvalidEmail();
        return email;
    }

    public interface User extends PrimaryActor<AppTrigger.Event, Void> {

        String askToEnterEmail() throws InterruptedException;

        String askToEnterPassword() throws InterruptedException;

        String askToEnterMobile() throws InterruptedException;

        void handle(Exception e);
    }
}
