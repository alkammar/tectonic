package com.morkim.usecase.di;

public class AppInjector {

    private static AppComponent appComponent;

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    public static void setAppComponent(AppComponent appComponent) {
        AppInjector.appComponent = appComponent;
    }
}
