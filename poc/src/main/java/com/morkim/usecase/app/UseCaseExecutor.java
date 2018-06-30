package com.morkim.usecase.app;

import com.morkim.tectonic.usecase.Builder;
import com.morkim.tectonic.usecase.PreconditionActor;
import com.morkim.tectonic.usecase.PrimaryActor;
import com.morkim.tectonic.usecase.Triggers;
import com.morkim.usecase.di.AppInjector;
import com.morkim.usecase.di.flow.DaggerSecondaryFlowComponent;
import com.morkim.usecase.di.flow.SecondaryFlowModule;
import com.morkim.usecase.di.uc.login.DaggerLoginUserComponent;
import com.morkim.usecase.di.uc.login.LoginUserModule;
import com.morkim.usecase.di.uc.logout.DaggerLogoutUserComponent;
import com.morkim.usecase.di.uc.logout.LogoutUserModule;
import com.morkim.usecase.di.uc.main.DaggerMainUseCaseComponent;
import com.morkim.usecase.di.uc.main.MainUseCaseModule;
import com.morkim.usecase.di.uc.secondary.DaggerSecondaryUseCaseComponent;
import com.morkim.usecase.di.uc.secondary.SecondaryUseCaseModule;
import com.morkim.usecase.uc.MainUseCase;

public class UseCaseExecutor implements Triggers<UseCaseExecutor.Event> {

    @Override
    public Event trigger(Event event) {
        return trigger(event, null, null);
    }

    @Override
    public Event trigger(Event event, PrimaryActor<Event, ?> primaryActor) {
        return trigger(event, null, primaryActor);
    }

    @Override
    public Event trigger(Event event, PreconditionActor<Event> preconditionActor) {
        return trigger(event, preconditionActor, null);
    }

    @Override
    public Event trigger(Event event, PreconditionActor<Event> preconditionActor, PrimaryActor<Event, ?> primaryActor) {

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

                execute(event, preconditionActor, AppInjector.getMainScreenComponent().ui());
                break;
            case REFRESH_AUTH:

                AppInjector.setLoginUserComponent(
                        DaggerLoginUserComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .loginUserModule(new LoginUserModule())
                                .build());

                execute(event, preconditionActor, primaryActor);
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
                                .secondaryUseCaseModule(new SecondaryUseCaseModule(AppInjector.getSecondaryFlowComponent().ui()))
                                .build());

                execute(event, preconditionActor, AppInjector.getSecondaryFlowComponent().ui());
                break;

            case USER_LOGOUT:

                AppInjector.setLogoutUserComponent(
                        DaggerLogoutUserComponent.builder()
                                .appComponent(AppInjector.getAppComponent())
                                .logoutUserModule(new LogoutUserModule())
                                .build());

                execute(event, preconditionActor, primaryActor);

                break;
        }

        return event;
    }

    private void execute(Event event, PreconditionActor<Event> preconditionActor, PrimaryActor<Event, ?> primaryActor) {
        new Builder<Event>()
                .useCase(MainUseCase.class)
                .primaryActor(primaryActor)
                .preconditionActor(preconditionActor)
                .triggers(this)
                .build()
                .execute(event);
    }

    public enum Event {
        LAUNCH_MAIN,
        REFRESH_MAIN,
        REFRESH_AUTH,
        USER_LOGOUT,
        DO_SECONDARY_THING,
        PRE_CONDITION_MAIN,
    }
}
