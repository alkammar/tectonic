package com.morkim.usecase.di;

import android.content.Context;

import com.morkim.usecase.app.App;
import com.morkim.usecase.model.Profile;

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
}
