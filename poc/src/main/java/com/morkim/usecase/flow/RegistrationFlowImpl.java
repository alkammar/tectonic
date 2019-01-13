package com.morkim.usecase.flow;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.UseCaseHandle;
import com.morkim.tectonic.usecase.Random;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.contract.Registration;
import com.morkim.usecase.uc.RegisterUser;
import com.morkim.usecase.uc.ValidationException;

import java.util.UUID;

import javax.inject.Inject;

public class RegistrationFlowImpl
        implements Registration.Flow,
        RegisterUser.UI<UseCaseExecutor.Event> {

    private static final UUID EMAIL = UUID.randomUUID();
    private static final UUID PASSWORD = UUID.randomUUID();
    private static final UUID PASSWORD_CONFIRM = UUID.randomUUID();
    private static final UUID NEXT = UUID.randomUUID();

    private static final UUID MOBILE = UUID.randomUUID();
    private static final UUID CONFIRM = UUID.randomUUID();

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
    public void onStart(UseCaseExecutor.Event event, UseCaseHandle handle) {
        this.handle = handle;
    }

    @Override
    public Random<String> askForEmail() throws InterruptedException {
        if (step1 == null) step1 = stepFactory.create(Registration.Step1.class);
        return handle.waitForRandom(EMAIL);
    }

    @Override
    public Random<String> askForPassword() throws InterruptedException {
        return handle.waitForRandom(PASSWORD);
    }

    @Override
    public Random<String> askForPasswordConfirmation() throws InterruptedException {
        return handle.waitForRandom(PASSWORD_CONFIRM);
    }

    @Override
    public void askForConfirmation() throws InterruptedException, UndoException {
        if (!validStep1) handle.waitForSafe(this, step1, NEXT);
        else handle.waitForSafe(this, step2, CONFIRM);
    }

    @Override
    public void confirmRegistrationError() {

    }

    @Override
    public void indicatePasswordStrength(int strength) {
        step1.updatePasswordStrength(strength);
    }

    @Override
    public Random<String> askForMobile() throws InterruptedException {

        if (validStep1) {
            if (step2 == null) step2 = stepFactory.create(Registration.Step2.class);
            return handle.waitForRandom(MOBILE);
        } else {
            return new Random<>("");
        }
    }

    @Override
    public void submitPassword(String password) {
        handle.replyWithRandom(step1, PASSWORD, new Random<>(password));
    }

    @Override
    public void next(String email, String password, String passwordConfirm) {
        handle.replyWith(step1, EMAIL, new Random<>(email));
        handle.replyWith(step1, PASSWORD, new Random<>(password));
        handle.replyWith(step1, PASSWORD_CONFIRM, new Random<>(passwordConfirm));
        handle.replyWithRandom(step1, NEXT);
    }

    @Override
    public void submit(String mobile) {
        handle.replyWith(step2, MOBILE, new Random<>(mobile));
        handle.replyWithRandom(step2, CONFIRM);
    }

    @Override
    public void showError(ValidationException e) {

        int error = e.getError();

        int e1 = error & RegisterUser.UI.ERROR_MISSING_INPUT;
        int e2 = error & RegisterUser.UI.ERROR_EMPTY_EMAIL;
        int e3 = error & RegisterUser.UI.ERROR_INVALID_EMAIL;
        int e4 = error & RegisterUser.UI.ERROR_EMPTY_PASSWORD;
        int e5 = error & RegisterUser.UI.ERROR_NON_MATCHING_PASSWORDS;

        if (e2 != RegisterUser.UI.OK) { step1.showError(e2); }
        if (e3 != RegisterUser.UI.OK) { step1.showError(e3); }
        if (e4 != RegisterUser.UI.OK) { step1.showError(e4); }
        if (e5 != RegisterUser.UI.OK) { step1.showError(e5); }

        validStep1 = (e1 | e2 | e3 | e4 | e5) == RegisterUser.UI.OK;
        if (!validStep1) handle.clear(NEXT);

        int e6 = error & RegisterUser.UI.ERROR_EMPTY_MOBILE;

        if (validStep1 && step2 != null) if (e6 != RegisterUser.UI.OK) { step2.showError(e6); }

        boolean validStep2 = e6 == RegisterUser.UI.OK;
        if (!validStep2) handle.clear(CONFIRM);
    }

    @Override
    public void goBack(Step step) {
        if (step == step1) handle.abort();
        else if (step == step2) handle.undo();
    }

    @Override
    public void onComplete(UseCaseExecutor.Event event, Void result) {

        step2.terminate();
        step1.terminate();
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
    }
}
