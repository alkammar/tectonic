package com.morkim.usecase.app;

import android.app.Application;
import android.content.Intent;

import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.AppModule;
import com.morkim.usecase.di.DaggerAppComponent;
import com.morkim.usecase.uc.logout.LogoutUser;
import com.morkim.usecase.ui.main.MainActivity;


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
