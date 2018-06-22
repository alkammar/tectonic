package com.morkim.usecase.di;

import com.morkim.usecase.di.ui.MainScreenComponent;

public class AppInjector {

    private static AppComponent appComponent;
    private static MainScreenComponent mainScreenComponent;

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    public static void setAppComponent(AppComponent appComponent) {
        AppInjector.appComponent = appComponent;
    }

    public static MainScreenComponent getMainScreenComponent() {
        return mainScreenComponent;
    }

    public static void setMainScreenComponent(MainScreenComponent mainScreenComponent) {
        AppInjector.mainScreenComponent = mainScreenComponent;
    }
}
