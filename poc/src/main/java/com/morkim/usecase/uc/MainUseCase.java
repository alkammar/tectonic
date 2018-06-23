package com.morkim.usecase.uc;

import android.os.SystemClock;

import com.morkim.tectonic.simplified.PrimaryActor;
import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.di.AppInjector;

import javax.inject.Inject;


public class MainUseCase extends UseCase<AppTrigger.Event, String> {

    private static final int STEP = 1;

    @Inject
    User user;

    @Override
    protected void onCreate() {
        super.onCreate();

        AppInjector.getMainUseCaseComponent().inject(this);
    }

    @Override
    protected boolean onCheckPreconditions() {

        triggerPreconditions(AppTrigger.Event.PRECONDITION_LOGIN);

        return true;
    }

    @Override
    protected void onExecute() {

        for (int i = 0; i < 100 / STEP; i++) {
            SystemClock.sleep(50);

            user.updateResult("" + (i + 1) * STEP);
        }

        complete("Final result sent by the main use case");
    }

    public interface Authenticator {

        boolean checkLogin();
    }

    public interface User extends PrimaryActor<AppTrigger.Event, String> {

        void updateResult(String data);
    }
}
