package com.morkim.usecase.auth;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.tectonic.usecase.UseCaseHandle;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.contract.Login;
import com.morkim.usecase.uc.login.LoginUser;
import com.morkim.usecase.uc.main.MainUseCase;
import com.morkim.usecase.uc.secondary.SecondaryUseCase;

import javax.inject.Inject;

public class AuthenticationFlow
        implements
        LoginUser.UI,
        Login.Flow,
        MainUseCase.Authenticator,
        SecondaryUseCase.Authenticator {

    private static final int REFRESH = 51;
    private static final int PASSWORD = 1;

    private final StepFactory stepFactory;
    private Login.Screen login;

    private Triggers<UseCaseExecutor.Event> triggers;

    @Inject
    public AuthenticationFlow(StepFactory stepFactory, Triggers<UseCaseExecutor.Event> triggers) {
        this.stepFactory = stepFactory;
        this.triggers = triggers;
    }

    @Override
    public void refreshAuthentication() throws InterruptedException {

        triggers.trigger(UseCaseExecutor.Event.REFRESH_AUTH, this);

        UseCase.waitFor(REFRESH);
    }

    @Override
    public void onStart(UseCaseHandle handle) {

    }

    @Override
    public String askForPassword() throws InterruptedException {
        if (login == null) login = stepFactory.create(Login.Screen.class);
        return UseCase.waitFor(PASSWORD);
    }

    @Override
    public void submit(String password) {
        UseCase.replyWith(PASSWORD, password);
    }

    @Override
    public void show(Exception e) {
        UseCase.clear(PASSWORD);
        login.handle(e);
    }

    @Override
    public void onComplete(UseCaseExecutor.Event event, Void result) {
        switch (event) {
            case REFRESH_AUTH:
                login.terminate();
                login = null;
                UseCase.replyWith(REFRESH);
                UseCase.clear(PASSWORD);
                UseCase.clear(REFRESH);
                break;
        }
    }

    @Override
    public void onUndo(Step step) {

    }

    @Override
    public void onAbort(UseCaseExecutor.Event event) {

    }
}
