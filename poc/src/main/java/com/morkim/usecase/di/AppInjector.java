package com.morkim.usecase.di;

import com.morkim.usecase.di.flow.RegistrationFlowComponent;
import com.morkim.usecase.di.flow.SecondaryFlowComponent;
import com.morkim.usecase.di.uc.login.LoginUserComponent;
import com.morkim.usecase.di.uc.logout.LogoutUserComponent;
import com.morkim.usecase.di.uc.main.MainUseCaseComponent;
import com.morkim.usecase.di.uc.regsitration.RegisterUserComponent;
import com.morkim.usecase.di.uc.secondary.SecondaryUseCaseComponent;
import com.morkim.usecase.di.ui.login.LoginScreenComponent;
import com.morkim.usecase.di.ui.main.MainScreenComponent;
import com.morkim.usecase.di.ui.registration.Registration1ActivityComponent;
import com.morkim.usecase.di.ui.registration.Registration2ActivityComponent;
import com.morkim.usecase.di.ui.secondary.SecondaryScreen1Component;
import com.morkim.usecase.di.ui.secondary.SecondaryScreen2Component;
import com.morkim.usecase.di.ui.secondary.SecondaryScreen3Component;

public class AppInjector {

    private static AppComponent appComponent;

    private static MainScreenComponent mainScreenComponent;
    private static MainUseCaseComponent mainUseCaseComponent;

    private static LoginUserComponent loginUserComponent;
    private static LoginScreenComponent loginScreenComponent;
    private static LogoutUserComponent logoutUserComponent;

    private static SecondaryUseCaseComponent secondaryUseCaseComponent;
    private static SecondaryFlowComponent secondaryFlowComponent;
    private static SecondaryScreen1Component secondaryScreen1Component;
    private static SecondaryScreen2Component secondaryScreen2Component;
    private static SecondaryScreen3Component secondaryScreen3Component;

    private static RegistrationFlowComponent registrationFlowComponent;
    private static RegisterUserComponent registerUserComponent;

    private static Registration1ActivityComponent registration1ActivityComponent;
    private static Registration2ActivityComponent registration2ActivityComponent;

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

    public static LogoutUserComponent getLogoutUserComponent() {
        return logoutUserComponent;
    }

    public static void setLogoutUserComponent(LogoutUserComponent logoutUserComponent) {
        AppInjector.logoutUserComponent = logoutUserComponent;
    }

    public static SecondaryUseCaseComponent getSecondaryUseCaseComponent() {
        return secondaryUseCaseComponent;
    }

    public static void setSecondaryUseCaseComponent(SecondaryUseCaseComponent secondaryUseCaseComponent) {
        AppInjector.secondaryUseCaseComponent = secondaryUseCaseComponent;
    }

    public static void setSecondaryFlowComponent(SecondaryFlowComponent secondaryFlowComponent) {
        AppInjector.secondaryFlowComponent = secondaryFlowComponent;
    }

    public static SecondaryFlowComponent getSecondaryFlowComponent() {
        return secondaryFlowComponent;
    }

    public static SecondaryScreen1Component getSecondaryScreen1Component() {
        return secondaryScreen1Component;
    }

    public static void setSecondaryScreen1Component(SecondaryScreen1Component secondaryScreen1Component) {
        AppInjector.secondaryScreen1Component = secondaryScreen1Component;
    }

    public static SecondaryScreen2Component getSecondaryScreen2Component() {
        return secondaryScreen2Component;
    }

    public static void setSecondaryScreen2Component(SecondaryScreen2Component secondaryScreen2Component) {
        AppInjector.secondaryScreen2Component = secondaryScreen2Component;
    }

    public static SecondaryScreen3Component getSecondaryScreen3Component() {
        return secondaryScreen3Component;
    }

    public static void setSecondaryScreen3Component(SecondaryScreen3Component secondaryScreen3Component) {
        AppInjector.secondaryScreen3Component = secondaryScreen3Component;
    }

    public static RegistrationFlowComponent getRegistrationFlowComponent() {
        return registrationFlowComponent;
    }

    public static void setRegistrationFlowComponent(RegistrationFlowComponent registrationFlowComponent) {
        AppInjector.registrationFlowComponent = registrationFlowComponent;
    }

    public static RegisterUserComponent getRegisterUserComponent() {
        return registerUserComponent;
    }

    public static void setRegisterUserComponent(RegisterUserComponent registerUserComponent) {
        AppInjector.registerUserComponent = registerUserComponent;
    }

    public static Registration1ActivityComponent getRegistration1ActivityComponent() {
        return registration1ActivityComponent;
    }

    public static void setRegistration1ActivityComponent(Registration1ActivityComponent registration1ActivityComponent) {
        AppInjector.registration1ActivityComponent = registration1ActivityComponent;
    }

    public static Registration2ActivityComponent getRegistration2ActivityComponent() {
        return registration2ActivityComponent;
    }

    public static void setRegistration2ActivityComponent(Registration2ActivityComponent registration2ActivityComponent) {
        AppInjector.registration2ActivityComponent = registration2ActivityComponent;
    }
}
