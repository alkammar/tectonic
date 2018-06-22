package com.morkim.usecase.app;

import android.app.Application;

import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.AppModule;
import com.morkim.usecase.di.DaggerAppComponent;
import com.morkim.usecase.di.ui.DaggerMainScreenComponent;
import com.morkim.usecase.di.ui.MainScreenModule;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AppInjector.setAppComponent(
                DaggerAppComponent.builder()
                        .appModule(new AppModule(this))
                        .build());

        AppInjector.setMainScreenComponent(
                DaggerMainScreenComponent.builder()
                        .appComponent(AppInjector.getAppComponent())
                        .mainScreenModule(new MainScreenModule())
                        .build());
    }
}
