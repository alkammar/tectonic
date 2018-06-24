package com.morkim.usecase.di;

import android.content.Context;
import android.content.Intent;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.simplified.Triggers;
import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.app.App;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.contract.login.Login;
import com.morkim.usecase.model.Profile;
import com.morkim.usecase.ui.login.LoginActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final App application;

    public AppModule(App app) {
        application = app;
    }

    @Provides
    Context provideContext() {
        return application;
    }

    @Singleton
    @Provides
    Profile provideProfile() {
        return new Profile();
    }

    @Singleton
    @Provides
    Triggers<AppTrigger.Event> provideTriggers() {
        return new AppTrigger();
    }

    @Singleton
    @Provides
    StepFactory provideStepFactory() {
        return new StepFactory() {
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
                UseCase.replyWith(100, step);
            }

            private <S> S createActivity(Class<?> cls) throws InterruptedException {
                application.startActivity(new Intent(application, cls));
                return UseCase.waitFor(100);
            }
        };
    }
}
