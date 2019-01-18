package com.morkim.usecase.auth;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCaseHandle;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.tectonic.usecase.UnexpectedStep;
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
        LoginUser.UI<UseCaseExecutor.Event>,
        Login.Flow,
        MainUseCase.Authenticator<UseCaseExecutor.Event>,
        SecondaryUseCase.Authenticator<UseCaseExecutor.Event> {

    private static final UUID REFRESH = UUID.randomUUID();
    private static final UUID PASSWORD = UUID.randomUUID();

    private final StepFactory stepFactory;
    private Login.Screen login;

    private Triggers<UseCaseExecutor.Event> triggers;
    private UseCaseHandle handle;

    @Inject
    public AuthenticationFlow(StepFactory stepFactory, Triggers<UseCaseExecutor.Event> triggers) {
        this.stepFactory = stepFactory;
        this.triggers = triggers;
    }

    @Override
    public void refreshAuthentication() throws InterruptedException, UndoException {

        triggers.trigger(UseCaseExecutor.Event.REFRESH_AUTH, this);

        try {
            handle.waitFor(this, login, REFRESH);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(UseCaseExecutor.Event event, UseCaseHandle handle) {
        this.handle = handle;
    }

    @Override
    public String askForPassword() throws InterruptedException, UnexpectedStep {
        if (login == null) login = stepFactory.create(Login.Screen.class);
        return handle.waitFor(this, login, PASSWORD, UserWantsToRegister.class);
    }

    @Override
    public void submit(String password) {
        handle.replyWith(login, PASSWORD, password);
    }

    @Override
    public void notRegistered() {
        handle.replyWith(login, PASSWORD, new UserWantsToRegister());
    }

    @Override
    public void show(Exception e) {
        handle.clear(PASSWORD);
        login.handle(e);
    }

    @Override
    public void onComplete(UseCaseExecutor.Event event, Void result) {
        switch (event) {
            case REFRESH_AUTH:
                login.terminate();
                login = null;
                break;
        }
    }

    @Override
    public void onUndo(Step step, boolean inclusive) {

    }

    @Override
    public void onAbort(UseCaseExecutor.Event event) {

    }
}
