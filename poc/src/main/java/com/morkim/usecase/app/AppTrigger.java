package com.morkim.usecase.app;

import com.morkim.tectonic.simplified.Triggers;
import com.morkim.tectonic.simplified.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.uc.login.DaggerLoginUserComponent;
import com.morkim.usecase.di.uc.login.LoginModule;
import com.morkim.usecase.di.uc.main.DaggerMainUseCaseComponent;
import com.morkim.usecase.di.uc.main.MainUseCaseModule;
import com.morkim.usecase.uc.LoginUser;
import com.morkim.usecase.uc.MainUseCase;

public class AppTrigger implements Triggers<AppTrigger.Event> {

    @Override
    public void trigger(Event event) {

        switch (event) {

            case LAUNCH_MAIN:

                AppInjector.setMainUseCaseComponent(
                        DaggerMainUseCaseComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .mainUseCaseModule(new MainUseCaseModule(AppInjector.getMainScreenComponent().user()))
                                .build()
                );

                UseCase.fetch(MainUseCase.class)
                        .setTriggers(AppInjector.getAppComponent().triggers())
                        .execute(event);
                break;
            case USER_LOGOUT:
                break;
            case PRECONDITION_LOGIN:

                AppInjector.setLoginUserComponent(
                        DaggerLoginUserComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .loginModule(new LoginModule())
                                .build());

                UseCase.fetch(LoginUser.class)
                        .setPrimaryActor(AppInjector.getLoginUserComponent().primaryActor())
                        .execute(event);
                break;
        }
    }

    public enum Event {
        LAUNCH_MAIN,
        USER_LOGOUT,
        PRECONDITION_LOGIN,
    }
}
