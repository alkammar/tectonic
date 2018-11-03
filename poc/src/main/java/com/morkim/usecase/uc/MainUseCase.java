package com.morkim.usecase.uc;

import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.app.UseCaseExecutor;
import com.morkim.usecase.di.AppInjector;

import javax.inject.Inject;

import lib.morkim.uc.ExpiredCredentials;


public class MainUseCase extends UseCase<String> {

    private static final int STEP = 1;

    @Inject
    Backend backend;

    @Inject
    Authenticator authenticator;

    @Inject
    UI ui;

    @Override
    protected void onCreate() {
        super.onCreate();

        AppInjector.getMainUseCaseComponent().inject(this);
    }

    @Override
    protected void onExecute() throws InterruptedException {

        try {
            String someData = backend.retrieveSomeData();

            for (int i = 0; i < 100 / STEP; i++) {
                Thread.sleep(50);

                ui.updateResult("" + (i + 1) * STEP);
            }

            complete("Final result sent by the main use case\n" + someData);

        } catch (ExpiredCredentials e) {
            authenticator.refreshAuthentication();
            retry();
        }
    }

    public interface Backend {

        String retrieveSomeData() throws ExpiredCredentials;
    }

    public interface Authenticator {

        void refreshAuthentication() throws InterruptedException;
    }

    public interface UI extends PrimaryActor<UseCaseExecutor.Event, String> {

        void updateResult(String data);
    }
}
