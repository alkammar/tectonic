package com.morkim.usecase.app;

import android.content.Intent;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.contract.Login;
import com.morkim.usecase.contract.Logout;
import com.morkim.usecase.ui.login.LoginActivity;
import com.morkim.usecase.ui.main.MainActivity;

public class StepFactoryImpl implements StepFactory {

    private final PoC application;

    public StepFactoryImpl(PoC poC) {
        application = poC;
    }

    @Override
    public <S> S create(Class<S> aClass) {
        try {
            if (aClass == Login.Screen.class)
                return createActivity(LoginActivity.class);
            if (aClass == Logout.LoginScreen.class) {
                return createActivity(MainActivity.class,
                        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public <S> void onCreated(S step) {
        UseCase.replyWith(step.getClass().hashCode(), step);
        UseCase.clear(step.getClass().hashCode());
    }

    private <S> S createActivity(Class<?> cls) throws InterruptedException {
        return createActivity(cls, 0);
    }

    private <S> S createActivity(Class<?> cls, int flags) throws InterruptedException {
        application.startActivity(new Intent(application, cls)
                .setFlags(flags));
        return UseCase.waitFor(cls.hashCode());
    }
}
