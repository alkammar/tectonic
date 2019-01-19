package com.morkim.usecase.flow;

import com.morkim.tectonic.flow.Step;
import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.UseCaseHandle;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.contract.Logout;
import com.morkim.usecase.uc.LogoutUser;

import javax.inject.Inject;

public class LogoutFlowImpl
        implements Logout.Flow,
        LogoutUser.UI<UseCaseExecutor.Event> {

    private final StepFactory stepFactory;

    @Inject
    public LogoutFlowImpl(StepFactory stepFactory) {

        this.stepFactory = stepFactory;
    }

    @Override
    public void onStart(UseCaseExecutor.Event event, UseCaseHandle handle) {

    }

    @Override
    public void showLogin() {
        stepFactory.create(Logout.LoginScreen.class);
    }

    @Override
    public void onUndo(Step step, boolean inclusive) {

    }

    @Override
    public void onComplete(UseCaseExecutor.Event event) {

    }

    @Override
    public void onAbort(UseCaseExecutor.Event event) {

    }
}
