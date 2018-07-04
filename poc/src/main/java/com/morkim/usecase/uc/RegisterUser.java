package com.morkim.usecase.uc;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.Random;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.di.AppInjector;

import java.util.Objects;

import javax.inject.Inject;


public class RegisterUser extends UseCase {

    @Inject
    UI ui;

    @Inject
    Backend backend;

    @Override
    protected void onCreate() {
        super.onCreate();

        AppInjector.getRegisterUserComponent().inject(this);
    }

    @Override
    protected void onExecute() throws InterruptedException {

        try {

            Random<String> email = ui.askForEmail();
            validateEmail(email);

            Random<String> password = ui.askForPassword();
            validatePassword(password);
            ui.indicatePasswordStrength(strength(password.value()));

            Random<String> passwordConfirmation = ui.askForPasswordConfirmation();
            validatePasswordConfirmation(password, passwordConfirmation);

            Random<String> mobile = ui.askForMobile();
            validateMobile(mobile);

            backend.register(email.value(), password.value(), mobile.value());

            complete();

        } catch (ValidationException e) {
            ui.showError(e);
            restart();
        }
    }

    private int strength(String password) {
        return password != null ? password.length() : 0;
    }

    private void validateEmail(Random<String> email) throws EmptyEmail, InvalidEmail {
        if (!email.isSet()) return;
        if (email.value().isEmpty()) throw new EmptyEmail();
        if (!email.value().contains("@")) throw new InvalidEmail();
    }

    private void validatePassword(Random<String> password) throws EmptyPassword {
        if (!password.isSet()) return;
        if (password.value().isEmpty()) throw new EmptyPassword();
    }

    private void validatePasswordConfirmation(Random<String> password, Random<String> passwordConfirmation) throws NonMatchingPasswords {
        if (!passwordConfirmation.isSet()) return;
        if (password.value() == null) return;
        if (!Objects.equals(password.value(), passwordConfirmation.value()))
            throw new NonMatchingPasswords();
    }

    private void validateMobile(Random<String> mobile) throws EmptyMobile {
        if (!mobile.isSet()) return;
        if (mobile.value().isEmpty()) throw new EmptyMobile();
    }

    public interface UI extends PrimaryActor<UseCaseExecutor.Event, Void> {

        int ERROR_EMPTY_EMAIL = -1;
        int ERROR_INVALID_EMAIL = -2;
        int ERROR_EMPTY_PASSWORD = -3;
        int ERROR_NON_MATCHING_PASSWORDS = -4;
        int ERROR_EMPTY_MOBILE = -5;

        Random<String> askForEmail() throws InterruptedException;

        Random<String> askForPassword() throws InterruptedException;

        Random<String> askForPasswordConfirmation() throws InterruptedException;

        void indicatePasswordStrength(int strength);

        Random<String> askForMobile() throws InterruptedException;

        void showError(Exception e);
    }

    public interface Backend {

        void register(String email, String password, String mobile);
    }
}
