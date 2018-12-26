package com.morkim.usecase.auth;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.PrimaryHandle;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.tectonic.usecase.UnexpectedStep;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.contract.Login;
import com.morkim.usecase.uc.LoginUser;
import com.morkim.usecase.uc.MainUseCase;
import com.morkim.usecase.uc.UserWantsToRegister;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import lib.morkim.uc.SecondaryUseCase;

public class AuthenticationFlow
        implements
        LoginUser.UI,
        Login.Flow,
        MainUseCase.Authenticator,
        SecondaryUseCase.Authenticator {

    private static final UUID REFRESH = UUID.randomUUID();
    private static final UUID PASSWORD = UUID.randomUUID();

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

        try {
            UseCase.waitFor(REFRESH);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(UseCaseExecutor.Event event, PrimaryHandle handle) {

    }

    @Override
    public String askForPassword() throws InterruptedException, UnexpectedStep {
        if (login == null) login = stepFactory.create(Login.Screen.class);
        return UseCase.waitFor(PASSWORD, UserWantsToRegister.class);
    }

    @Override
    public void submit(String password) {
        UseCase.replyWith(PASSWORD, password);
    }

    @Override
    public void notRegistered() {
        UseCase.replyWith(PASSWORD, new UserWantsToRegister());
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
