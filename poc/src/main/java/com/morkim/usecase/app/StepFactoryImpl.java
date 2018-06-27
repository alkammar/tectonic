package com.morkim.usecase.app;

import android.content.Intent;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.contract.login.Login;
import com.morkim.usecase.ui.login.LoginActivity;

public class StepFactoryImpl implements StepFactory {

    private final PoC application;

    public StepFactoryImpl(PoC poC) {
        application = poC;
    }

    @Override
    public <S> S create(Class<S> aClass) {
        try {
            if (aClass == Login.Screen.class) return createActivity(LoginActivity.class);
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
        application.startActivity(new Intent(application, cls));
        return UseCase.waitFor(cls.hashCode());
    }
}
