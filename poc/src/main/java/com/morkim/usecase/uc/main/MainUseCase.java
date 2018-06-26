package com.morkim.usecase.uc.main;

import android.os.SystemClock;

import com.morkim.tectonic.simplified.PrimaryActor;
import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.di.AppInjector;

import javax.inject.Inject;


public class MainUseCase extends UseCase<AppTrigger.Event, String> {

    private static final int STEP = 1;

    @Inject
    Backend backend;

    @Inject
    Authenticator authenticator;

    @Inject
    User user;

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
                SystemClock.sleep(50);

                user.updateResult("" + (i + 1) * STEP);
            }

            complete("Final result sent by the main use case\n" + someData);

        } catch (ExpiredCredentials e) {
            authenticator.refreshAuthentication();
            restart();
        }
    }

    public interface Backend {

        String retrieveSomeData() throws ExpiredCredentials;
    }

    public interface Authenticator {

        Boolean refreshAuthentication() throws InterruptedException;
    }

    public interface User extends PrimaryActor<AppTrigger.Event, String> {

        void updateResult(String data);
    }
}
