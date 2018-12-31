package com.morkim.usecase.app;

import com.morkim.tectonic.usecase.Builder;
import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.ResultActor;
import com.morkim.tectonic.usecase.TectonicEvent;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.tectonic.usecase.UseCase;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.flow.DaggerRegistrationFlowComponent;
import com.morkim.usecase.di.flow.DaggerSecondaryFlowComponent;
import com.morkim.usecase.di.flow.RegistrationFlowModule;
import com.morkim.usecase.di.flow.SecondaryFlowModule;
import com.morkim.usecase.di.uc.login.DaggerLoginUserComponent;
import com.morkim.usecase.di.uc.login.LoginUserModule;
import com.morkim.usecase.di.uc.logout.DaggerLogoutUserComponent;
import com.morkim.usecase.di.uc.logout.LogoutUserModule;
import com.morkim.usecase.di.uc.main.DaggerMainUseCaseComponent;
import com.morkim.usecase.di.uc.main.MainUseCaseModule;
import com.morkim.usecase.di.uc.regsitration.DaggerRegisterUserComponent;
import com.morkim.usecase.di.uc.regsitration.RegisterUserModule;
import com.morkim.usecase.di.uc.secondary.DaggerSecondaryUseCaseComponent;
import com.morkim.usecase.di.uc.secondary.SecondaryUseCaseModule;
import com.morkim.usecase.uc.LoginUser;
import com.morkim.usecase.uc.LogoutUser;
import com.morkim.usecase.uc.MainUseCase;
import com.morkim.usecase.uc.RegisterUser;

import lib.morkim.uc.SecondaryUseCase;

public class UseCaseExecutor implements Triggers<UseCaseExecutor.Event> {

    @Override
    public Event trigger(Event event) {
        return trigger(event, null, null, null, null);
    }

    @Override
    public <R> R trigger(Class<? extends UseCase<R>> cls, TectonicEvent contextEvent) {
        return null;
    }

    @Override
    public Event trigger(Event event, PrimaryActor primaryActor) {
        return trigger(event, null, primaryActor, null, null);
    }

    @Override
    public Event trigger(Event event, ResultActor resultActor) {
        return trigger(event, null, null, resultActor, null);
    }

    @Override
    public Event trigger(Class<? extends UseCase<?>> useCase, PreconditionActor preconditionActor) {

        if (MainUseCase.class.equals(useCase)) {
            return trigger(Event.PRE_CONDITION_MAIN, preconditionActor, null, null, null);
        }

        return null;
    }

    @Override
    public Event trigger(Event event, PreconditionActor preconditionActor, PrimaryActor primaryActor, ResultActor resultActor, Event contextEvent) {

        switch (event) {

            case LAUNCH_MAIN:
            case REFRESH_MAIN:
            case PRE_CONDITION_MAIN:

                AppInjector.setMainUseCaseComponent(
                        DaggerMainUseCaseComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .mainUseCaseModule(new MainUseCaseModule(AppInjector.getMainScreenComponent().ui()))
                                .build()
                );

                execute(MainUseCase.class, event, preconditionActor, AppInjector.getMainScreenComponent().ui(), resultActor);
                break;
            case REFRESH_AUTH:

                AppInjector.setLoginUserComponent(
                        DaggerLoginUserComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .loginUserModule(new LoginUserModule())
                                .build());

                execute(LoginUser.class, event, preconditionActor, primaryActor, resultActor);
                break;
            case DO_SECONDARY_THING:

                AppInjector.setSecondaryFlowComponent(
                        DaggerSecondaryFlowComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .secondaryFlowModule(new SecondaryFlowModule())
                                .build()
                );

                AppInjector.setSecondaryUseCaseComponent(
                        DaggerSecondaryUseCaseComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .secondaryUseCaseModule(new SecondaryUseCaseModule())
                                .build());

                execute(SecondaryUseCase.class, event, preconditionActor, AppInjector.getSecondaryFlowComponent().ui(), resultActor);
                break;

            case USER_LOGOUT:

                AppInjector.setLogoutUserComponent(
                        DaggerLogoutUserComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .logoutUserModule(new LogoutUserModule())
                                .build());

                execute(LogoutUser.class, event, preconditionActor, primaryActor, resultActor);

                break;
            case REGISTER:

                AppInjector.setRegistrationFlowComponent(
                        DaggerRegistrationFlowComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .registrationFlowModule(new RegistrationFlowModule())
                                .build()
                );

                AppInjector.setRegisterUserComponent(
                        DaggerRegisterUserComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .registerUserModule(new RegisterUserModule(AppInjector.getRegistrationFlowComponent().ui()))
                                .build());

                execute(RegisterUser.class, event, preconditionActor, AppInjector.getRegistrationFlowComponent().ui(), resultActor);

                break;
        }

        return event;
    }

    private void execute(Class<? extends UseCase> cls, TectonicEvent event, PreconditionActor preconditionActor, PrimaryActor primaryActor, ResultActor resultActor) {
        new Builder()
                .useCase(cls)
                .resultActor(resultActor)
                .preconditionActor(preconditionActor)
                .triggers(this)
                .build()
                .execute(event);
    }

    public enum Event implements TectonicEvent {
        LAUNCH_MAIN,
        REFRESH_MAIN,
        REFRESH_AUTH,
        USER_LOGOUT,
        DO_SECONDARY_THING,
        PRE_CONDITION_MAIN,
        REGISTER,
    }
}
