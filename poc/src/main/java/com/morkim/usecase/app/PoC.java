package com.morkim.usecase.app;

import android.app.Application;

import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.AppModule;
import com.morkim.usecase.di.DaggerAppComponent;


public class PoC extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppInjector.setAppComponent(
                DaggerAppComponent.builder()
                        .appModule(new AppModule(this))
                        .build());
    }
}
