package com.morkim.usecase.backend;

import android.os.SystemClock;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.usecase.ResultActor;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.tectonic.usecase.UndoException;
import com.morkim.tectonic.usecase.UseCaseHandle;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.uc.InvalidLogin;
import com.morkim.usecase.uc.LoginUser;
import com.morkim.usecase.uc.LogoutUser;
import com.morkim.usecase.uc.MainUseCase;
import com.morkim.usecase.uc.RegisterUser;

import java.util.UUID;

import javax.inject.Inject;

import lib.morkim.model.SecondaryModel;
import lib.morkim.uc.ExpiredCredentials;
import lib.morkim.uc.GeneralBackendError;
import lib.morkim.uc.SecondaryUseCase;
import lib.morkim.uc.SpecificBackendError;

public class BackendImpl
        implements MainUseCase.Backend<UseCaseExecutor.Event>,
        LogoutUser.Backend<UseCaseExecutor.Event>,
        SecondaryUseCase.Backend<UseCaseExecutor.Event>,
        LoginUser.Backend<UseCaseExecutor.Event>,
        ResultActor<UseCaseExecutor.Event, Void>,
        RegisterUser.Backend<UseCaseExecutor.Event> {

    private static final UUID REGISTRATION = UUID.randomUUID();
    private static boolean isExpired = true;
    private Triggers<UseCaseExecutor.Event> triggers;
    private UseCaseHandle handle;

    @Inject
    public BackendImpl(Triggers<UseCaseExecutor.Event> triggers) {
        this.triggers = triggers;
    }

    @Override
    public void onStart(UseCaseExecutor.Event event, UseCaseHandle handle) {
        this.handle = handle;
    }

    @Override
    public String retrieveSomeData() throws ExpiredCredentials {

        if (isExpired) {
            isExpired = false;
            throw new ExpiredCredentials();
        }

        return toString();
    }

    @Override
    public SecondaryModel requestSomething(String data1, String data2, double data3) throws ExpiredCredentials, GeneralBackendError, SpecificBackendError {

        if (isExpired) {
            isExpired = false;
            throw new ExpiredCredentials();
        }

        SystemClock.sleep(1000);

        if (data1.isEmpty()) throw new GeneralBackendError();
        if (data3 < 10) throw new SpecificBackendError();

        return new SecondaryModel();
    }

    @Override
    public boolean logout() {
        isExpired = true;
        return true;
    }

    @Override
    public void validateCredentials(String password) throws InvalidLogin {
        if (password.equals("asdf")) throw new InvalidLogin();
    }

    @Override
    public void register() throws InterruptedException, UndoException {
        triggers.trigger(UseCaseExecutor.Event.REGISTER, this);
        handle.waitForSafe(REGISTRATION);
    }

    @Override
    public void onComplete(UseCaseExecutor.Event event, Void result) {
        handle.replyWith(() -> {}, REGISTRATION);
    }

    @Override
    public void onAbort(UseCaseExecutor.Event event) {

    }

    @Override
    public void register(String email, String password, String mobile) {

    }

    @Override
    public void onUndo(Step step) {

    }
}
