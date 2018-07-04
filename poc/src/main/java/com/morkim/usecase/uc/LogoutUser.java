package com.morkim.usecase.uc;

import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.di.AppInjector;

import javax.inject.Inject;


public class LogoutUser extends UseCase {

    @Inject
    Backend backend;

    @Inject
    UI ui;

    @Override
    protected void onCreate() {
        super.onCreate();

        AppInjector.getLogoutUserComponent().inject(this);
    }

    @Override
    protected void onExecute() throws InterruptedException {

        backend.logout();
        ui.showLogin();

        complete();
    }

    public interface Backend {

        boolean logout();
    }

    public interface UI {

        void showLogin();
    }
}
