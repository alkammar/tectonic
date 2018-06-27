package com.morkim.usecase.flow;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.usecase.contract.Logout;
import com.morkim.usecase.uc.logout.LogoutUser;

import javax.inject.Inject;

public class LogoutFlowImpl implements Logout.Flow, LogoutUser.UI {

    private final StepFactory stepFactory;

    @Inject
    public LogoutFlowImpl(StepFactory stepFactory) {

        this.stepFactory = stepFactory;
    }

    @Override
    public void showLogin() {
        stepFactory.create(Logout.LoginScreen.class);
    }
}
