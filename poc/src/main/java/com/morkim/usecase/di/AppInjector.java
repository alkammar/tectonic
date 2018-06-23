package com.morkim.usecase.di;

import com.morkim.usecase.di.uc.login.LoginUserComponent;
import com.morkim.usecase.di.uc.main.MainUseCaseComponent;
import com.morkim.usecase.di.ui.MainScreenComponent;
import com.morkim.usecase.di.ui.login.LoginScreenComponent;

public class AppInjector {

    private static AppComponent appComponent;

    private static MainScreenComponent mainScreenComponent;
    private static MainUseCaseComponent mainUseCaseComponent;

    private static LoginUserComponent loginUserComponent;
    private static LoginScreenComponent loginScreenComponent;

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

    public static MainUseCaseComponent getMainUseCaseComponent() {
        return mainUseCaseComponent;
    }

    public static void setMainUseCaseComponent(MainUseCaseComponent mainUseCaseComponent) {
        AppInjector.mainUseCaseComponent = mainUseCaseComponent;
    }

    public static LoginUserComponent getLoginUserComponent() {
        return loginUserComponent;
    }

    public static void setLoginUserComponent(LoginUserComponent loginUserComponent) {
        AppInjector.loginUserComponent = loginUserComponent;
    }

    public static LoginScreenComponent getLoginScreenComponent() {
        return loginScreenComponent;
    }

    public static void setLoginScreenComponent(LoginScreenComponent loginScreenComponent) {
        AppInjector.loginScreenComponent = loginScreenComponent;
    }
}
