package com.morkim.usecase.di;

import android.content.Context;

import com.morkim.tectonic.flow.StepFactory;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.app.PoC;
import com.morkim.usecase.app.AppTrigger;
import com.morkim.usecase.app.StepFactoryImpl;
import com.morkim.usecase.auth.AuthenticationFlow;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private final PoC application;

    public AppModule(PoC poC) {
        application = poC;
    }

    @Provides
    @Singleton
    PoC providePoc() {
        return application;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return application;
    }

    @Singleton
    @Provides
    Triggers<AppTrigger.Event> provideTriggers() {
        return new AppTrigger();
    }

    @Singleton
    @Provides
    StepFactory provideStepFactory() {
        return new StepFactoryImpl(application);
    }

    @Provides
    @Singleton
    AuthenticationFlow provideAuthentication(StepFactory stepFactory, Triggers<AppTrigger.Event> triggers) {
        return new AuthenticationFlow(stepFactory, triggers);
    }
}
