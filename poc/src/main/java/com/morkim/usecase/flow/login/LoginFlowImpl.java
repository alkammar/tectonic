package com.morkim.usecase.flow.login;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.simplified.PrimaryActor;
import com.morkim.tectonic.simplified.UseCase;
import com.morkim.tectonic.simplified.UseCaseHandle;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.contract.login.Login;
import com.morkim.usecase.uc.login.LoginUser;

import javax.inject.Inject;

public class LoginFlowImpl implements Login.Flow, LoginUser.User, PrimaryActor<AppTrigger.Event, Void> {

    private static final int PASSWORD = 1;

    private final StepFactory stepFactory;
    private Login.Screen login;

    @Inject
    public LoginFlowImpl(StepFactory stepFactory) {

        this.stepFactory = stepFactory;
    }

    @Override
    public String askToEnterPassword() throws InterruptedException {
        if (login == null) login = stepFactory.create(Login.Screen.class);
        return UseCase.waitFor(PASSWORD);
    }

    @Override
    public void handle(Exception e) {
        UseCase.clear(PASSWORD);
        login.handle(e);
    }

    @Override
    public void submit(String password) {
        UseCase.replyWith(PASSWORD, password);
    }

    @Override
    public void onStart(UseCaseHandle handle) {

    }

    @Override
    public void onComplete(AppTrigger.Event event, Void result) {
        login.finish();
    }

    @Override
    public void onUndo(Step step) {

    }

    @Override
    public void onAbort(AppTrigger.Event event) {

    }
}
