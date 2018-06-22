package com.morkim.usecase.uc;

import com.morkim.tectonic.simplified.UseCase;

import javax.inject.Inject;


public class Login extends UseCase {

    @Inject
    Authenticator authenticator;

    @Inject
    User user;

    @Override
    protected boolean onCheckPreconditions() {
        return authenticator.checkRegistration();
    }

    @Override
    protected void onExecute() throws InterruptedException {

        String userName = user.askToEnterUserName();
        String password = user.askToEnterPassword();

        try {
            authenticator.validateCredentials(userName, password);
        } catch (InvalidLogin e) {
            user.handle(e);
            restart();
        }

        complete();
    }

    public interface Authenticator {

        boolean checkRegistration();

        void validateCredentials(String userName, String password) throws InvalidLogin;
    }

    public interface User {

        String askToEnterUserName() throws InterruptedException;

        String askToEnterPassword() throws InterruptedException;

        void handle(Exception e);
    }

}
