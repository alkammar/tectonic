package com.morkim.usecase.flow;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.Random;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.tectonic.usecase.UseCaseHandle;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.contract.Registration;
import com.morkim.usecase.uc.EmptyEmail;
import com.morkim.usecase.uc.EmptyMobile;
import com.morkim.usecase.uc.EmptyPassword;
import com.morkim.usecase.uc.InvalidEmail;
import com.morkim.usecase.uc.NonMatchingPasswords;
import com.morkim.usecase.uc.RegisterUser;
import com.morkim.usecase.uc.ValidationException;

import javax.inject.Inject;

public class RegistrationFlowImpl implements Registration.Flow, RegisterUser.UI {

    private static final int EMAIL = 81;
    private static final int PASSWORD = 82;
    private static final int PASSWORD_CONFIRM = 83;
    private static final int NEXT = 84;

    private static final int MOBILE = 85;
    private static final int CONFIRM = 86;

    private StepFactory stepFactory;

    private UseCaseHandle handle;

    private Registration.Step1 step1;
    private Registration.Step2 step2;

    private boolean validStep1;

    @Inject
    public RegistrationFlowImpl(StepFactory stepFactory) {
        this.stepFactory = stepFactory;
    }

    @Override
    public void onStart(UseCaseHandle handle) {
        this.handle = handle;
    }

    @Override
    public Random<String> askForEmail() throws InterruptedException {
        if (step1 == null) step1 = stepFactory.create(Registration.Step1.class);
        UseCase.waitFor(NEXT);
        return UseCase.waitForRandom(EMAIL, new Random<>(""));
    }

    @Override
    public Random<String> askForPassword() throws InterruptedException {
        return UseCase.waitFor(PASSWORD);
    }

    @Override
    public Random<String> askForPasswordConfirmation() throws InterruptedException {
        return UseCase.waitForRandom(PASSWORD_CONFIRM, new Random<>(""));
    }

    @Override
    public void indicatePasswordStrength(int strength) {
        step1.updatePasswordStrength(strength);
    }

    @Override
    public Random<String> askForMobile() throws InterruptedException {

        if (validStep1) {
            if (step2 == null) step2 = stepFactory.create(Registration.Step2.class);
            UseCase.clear(CONFIRM);
            UseCase.waitFor(CONFIRM);
            return UseCase.waitForRandom(MOBILE, new Random<>(""));
        } else {
            UseCase.clear(NEXT);
            validStep1 = true;
            throw new InterruptedException();
        }
    }

    @Override
    public void submitPassword(String password) {
        UseCase.replyWith(PASSWORD, new Random<>(password));
    }

    @Override
    public void next(String email, String password, String passwordConfirm) {
        UseCase.replyWith(EMAIL, new Random<>(email));
        UseCase.replyWith(PASSWORD, new Random<>(password));
        UseCase.replyWith(PASSWORD_CONFIRM, new Random<>(passwordConfirm));
        UseCase.replyWith(NEXT);
    }

    @Override
    public void submit(String mobile) {
        UseCase.replyWith(MOBILE, new Random<>(mobile));
        UseCase.replyWith(CONFIRM);
    }

    @Override
    public void showError(Exception e) {

        if (e instanceof EmptyEmail || e instanceof InvalidEmail) {
            validStep1 = false;
            step1.showError(e);
            UseCase.clear(EMAIL);
        } else if (e instanceof EmptyPassword) {
            validStep1 = false;
            step1.showError(e);
            UseCase.clear(PASSWORD);
        } else if (e instanceof NonMatchingPasswords) {
            validStep1 = false;
            step1.showError(e);
            UseCase.clear(PASSWORD_CONFIRM);
        } else if (e instanceof EmptyMobile) {
            if (step2 != null) step2.showError(e);
            UseCase.clear(MOBILE);
        } else if (e instanceof ValidationException) {
            UseCase.clear(CONFIRM);
        }
    }

    @Override
    public void goBack(Step step) {
        if (step == step1) handle.abort();
        else if (step == step2) handle.undo(step, MOBILE);
    }

    @Override
    public void onComplete(UseCaseExecutor.Event event, Void result) {

        step2.terminate();
        step1.terminate();

        UseCase.clear(EMAIL, PASSWORD, PASSWORD_CONFIRM, MOBILE);
    }

    @Override
    public void onUndo(Step step) {
        validStep1 = false;
        step.terminate();
        step2 = null;
    }

    @Override
    public void onAbort(UseCaseExecutor.Event event) {

        step2.terminate();
        step1.terminate();

        UseCase.clear(EMAIL, PASSWORD, PASSWORD_CONFIRM, MOBILE);
    }
}
