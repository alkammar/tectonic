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
            Random<String> password = ui.askForPassword();
            Random<String> passwordConfirmation = ui.askForPasswordConfirmation();
            Random<String> mobile = ui.askForMobile();

            ui.indicatePasswordStrength(strength(password.value()));

            ui.askForConfirmation();

            validate(email, password, passwordConfirmation, mobile);

            backend.register(email.value(), password.value(), mobile.value());

            complete();

        } catch (ValidationException e) {
            ui.showError(e);
            restart();
        } catch (UserAlreadyRegistered e) {
            ui.confirmRegistrationError();
        }
    }

    private void validate(Random<String> email, Random<String> password, Random<String> passwordConfirmation, Random<String> mobile) throws ValidationException {
        int error = UI.OK;
        error += validateEmail(email);
        error += validatePassword(password);
        error += validatePasswordConfirmation(password, passwordConfirmation);
        error += validateMobile(mobile);

        if (error != UI.OK) throw new ValidationException(error);
    }

    private int strength(String password) {
        return password != null ? password.length() : UI.OK;
    }

    private int validateEmail(Random<String> email) {
        if (!email.isSet()) return UI.OK;
        if (email.value().isEmpty()) return UI.ERROR_EMPTY_EMAIL;
        if (!email.value().contains("@")) return UI.ERROR_INVALID_EMAIL;
        return UI.OK;
    }

    private int validatePassword(Random<String> password) {
        if (!password.isSet()) return UI.OK;
        if (password.value().isEmpty()) return UI.ERROR_EMPTY_PASSWORD;
        return UI.OK;
    }

    private int validatePasswordConfirmation(Random<String> password, Random<String> passwordConfirmation) {
        if (!passwordConfirmation.isSet()) return UI.OK;
        if (password.value() == null) return UI.OK;
        if (!Objects.equals(password.value(), passwordConfirmation.value())) return UI.ERROR_NON_MATCHING_PASSWORDS;
        return UI.OK;
    }

    private int validateMobile(Random<String> mobile) {
        if (!mobile.isSet()) return UI.OK;
        if (mobile.value().isEmpty()) return UI.ERROR_EMPTY_MOBILE;
        return UI.OK;
    }

    public interface UI extends PrimaryActor<UseCaseExecutor.Event, Void> {

        int OK = 0;
        int ERROR_EMPTY_EMAIL = 1;
        int ERROR_INVALID_EMAIL = 1 << 1;
        int ERROR_EMPTY_PASSWORD = 1 << 2;
        int ERROR_NON_MATCHING_PASSWORDS = 1 << 3;
        int ERROR_EMPTY_MOBILE = 1 << 4;
        int ERROR_MISSING_INPUT = 1 << 5;

        Random<String> askForEmail() throws InterruptedException;

        Random<String> askForPassword() throws InterruptedException;

        Random<String> askForPasswordConfirmation() throws InterruptedException;

        void indicatePasswordStrength(int strength);

        Random<String> askForMobile() throws InterruptedException;

        void showError(ValidationException e);

        void askForConfirmation() throws InterruptedException;

        void confirmRegistrationError();
    }

    public interface Backend {

        void register(String email, String password, String mobile) throws UserAlreadyRegistered;
    }
}
