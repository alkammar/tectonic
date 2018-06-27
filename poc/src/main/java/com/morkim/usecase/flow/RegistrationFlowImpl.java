package com.morkim.usecase.flow;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.tectonic.usecase.UseCaseHandle;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.contract.RegistrationFlow;
import com.morkim.usecase.uc.registration.RegisterUser;

import javax.inject.Inject;

public class RegistrationFlowImpl implements RegistrationFlow.Flow, RegisterUser.User {

    private static final int EMAIL = 1;
    private static final int PASSWORD = 2;
    private static final int MOBILE = 3;

    @Inject
    StepFactory stepFactory;

    private UseCaseHandle handle;

    private RegistrationFlow.Step1 step1;
    private RegistrationFlow.Step2 step2;

    @Override
    public void onStart(UseCaseHandle handle) {
        this.handle = handle;
    }

    @Override
    public void onComplete(AppTrigger.Event event, Void result) {

    }

    @Override
    public String askToEnterEmail() throws InterruptedException {

        step1 = stepFactory.create(RegistrationFlow.Step1.class);

        return UseCase.waitFor(EMAIL);
    }

    @Override
    public String askToEnterPassword() throws InterruptedException {
        return UseCase.waitFor(PASSWORD);
    }

    @Override
    public String askToEnterMobile() throws InterruptedException {

        step2 = stepFactory.create(RegistrationFlow.Step2.class);

        return UseCase.waitFor(MOBILE);
    }

    @Override
    public void handle(Exception e) {
        step1.handle(e);
    }

    @Override
    public void submit(String email, String password) {
        UseCase.replyWith(EMAIL, email);
        UseCase.replyWith(PASSWORD, password);
    }

    @Override
    public void submit(String mobile) {
        UseCase.replyWith(MOBILE, mobile);
    }

    @Override
    public void goBack(Step step) {
        if (step == step1)
            handle.abort();
        else if (step == step2)
            handle.undo(step, MOBILE);
    }

    @Override
    public void onUndo(Step step) {
        step.terminate();
    }

    @Override
    public void onAbort(AppTrigger.Event event) {
        step2.terminate();
        step1.terminate();
    }
}
